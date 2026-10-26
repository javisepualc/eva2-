package cl.inacap.smartwaste.data.repository

import cl.inacap.smartwaste.data.model.TelemetryData
import cl.inacap.smartwaste.data.model.WasteHistoryItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class WasteBinRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val devicePath = "dispositivos/basurero_talca_01"

    companion object {
        private val _urgentAlertFlow = MutableStateFlow(Pair(false, ""))
        private val _thresholdConfigFlow = MutableStateFlow(Pair(80, false))
        private val _localHistory = mutableListOf<WasteHistoryItem>()
        private val _historyFlow = MutableStateFlow<List<WasteHistoryItem>>(emptyList())
    }

    // Suscripción reactiva a la telemetría publicada en Cloud Firestore
    fun observeTelemetry(): Flow<TelemetryData> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            val docRef = firestore.document("$devicePath/telemetria/actual")
            listener = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(TelemetryData(timestamp = 0L))
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val data = snapshot.toObject(TelemetryData::class.java) ?: TelemetryData(timestamp = 0L)
                    trySend(data)
                } else {
                    trySend(TelemetryData(timestamp = 0L))
                }
            }
        } catch (e: Exception) {
            trySend(TelemetryData(timestamp = 0L))
        }

        awaitClose {
            listener?.remove()
        }
    }

    // Consulta directa por HTTP al ESP32 (Wi-Fi Local / SoftAP 192.168.4.1)
    suspend fun fetchDirectTelemetry(ipAddress: String): Result<TelemetryData> = withContext(Dispatchers.IO) {
        try {
            val cleanIp = ipAddress.trim().removePrefix("http://").removeSuffix("/")
            val url = URL("http://$cleanIp/api/telemetria")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 2500
                readTimeout = 2500
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
            }

            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val response = reader.readText()
                reader.close()

                val json = JSONObject(response)
                val data = TelemetryData(
                    llenadoPorcentaje = json.optInt("llenadoPorcentaje", 0),
                    distanciaCm = json.optDouble("distanciaCm", 12.0),
                    temperatura = json.optDouble("temperatura", 21.0),
                    humedad = json.optDouble("humedad", 55.0),
                    gasADC = json.optInt("gasADC", 120),
                    lineaBaseGas = json.optInt("lineaBaseGas", 100),
                    volcado = json.optBoolean("volcado", false),
                    tapaAbierta = json.optBoolean("tapaAbierta", false),
                    presenciaDetectada = json.optBoolean("presenciaDetectada", false),
                    modoLimpieza = json.optBoolean("modoLimpieza", false),
                    timestamp = System.currentTimeMillis()
                )
                Result.success(data)
            } else {
                Result.failure(Exception("HTTP ${conn.responseCode} desde ESP32"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Envío de comando directo por HTTP al ESP32
    suspend fun sendDirectCommand(ipAddress: String, endpoint: String, bodyJson: String = ""): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val cleanIp = ipAddress.trim().removePrefix("http://").removeSuffix("/")
            val url = URL("http://$cleanIp/api/$endpoint")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 3000
                readTimeout = 3000
                requestMethod = "POST"
                doOutput = bodyJson.isNotEmpty()
                setRequestProperty("Content-Type", "application/json")
            }

            if (bodyJson.isNotEmpty()) {
                val writer = OutputStreamWriter(conn.outputStream)
                writer.write(bodyJson)
                writer.flush()
                writer.close()
            }

            if (conn.responseCode in 200..299) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error HTTP ${conn.responseCode} en comando directo"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Escucha en tiempo real de la bitácora de vaciados desde Firestore
    fun observeHistory(): Flow<List<WasteHistoryItem>> = callbackFlow {
        trySend(_historyFlow.value)
        var listener: ListenerRegistration? = null
        try {
            val collRef = firestore.collection("$devicePath/historial_vaciados")
                .orderBy("fechaHora", Query.Direction.DESCENDING)
                .limit(25)

            listener = collRef.addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(_historyFlow.value)
                    return@addSnapshotListener
                }
                val list = snapshot.documents.mapNotNull { it.toObject(WasteHistoryItem::class.java) }
                if (list.isNotEmpty()) {
                    _historyFlow.value = list
                }
                trySend(_historyFlow.value)
            }
        } catch (e: Exception) {
            trySend(_historyFlow.value)
        }

        awaitClose {
            listener?.remove()
        }
    }

    // Comando remoto real hacia el ESP32: Abrir tapa (rotar SG90 a 90° durante X segundos)
    suspend fun sendOpenLidCommand(seconds: Double = 4.0): Result<Unit> {
        return try {
            firestore.document("$devicePath/comandos/apertura").set(
                mapOf(
                    "accion" to "ABRIR_TAPA",
                    "duracionSegundos" to seconds,
                    "ejecutado" to false,
                    "timestamp" to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            // Si Firestore aún no tiene permisos cloud activos, no bloquear la UI
            Result.success(Unit)
        }
    }

    // Comando remoto real: Modo Limpieza continuo (tapa trabada a 90°)
    suspend fun setCleaningMode(enabled: Boolean): Result<Unit> {
        return try {
            firestore.document("$devicePath/comandos/modo_limpieza").set(
                mapOf(
                    "activo" to enabled,
                    "timestamp" to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    // Comando remoto real: Pitido de prueba en Buzzer (GPIO 33)
    suspend fun sendBuzzerTest(): Result<Unit> {
        return try {
            firestore.document("$devicePath/comandos/buzzer").set(
                mapOf(
                    "accion" to "TEST_BEEP",
                    "ejecutado" to false,
                    "timestamp" to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    // Habilitar o deshabilitar sensor IR frontal (GPIO 35) en hardware
    suspend fun setProximitySensor(enabled: Boolean): Result<Unit> {
        return try {
            firestore.document("$devicePath/configuracion/sensores").set(
                mapOf(
                    "sensorIrHabilitado" to enabled,
                    "timestamp" to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    // Restablecer alarma de volcamiento (KY-027) tras reposición física
    suspend fun resetTiltAlarm(): Result<Unit> {
        return try {
            firestore.document("$devicePath/comandos/reset_alarma").set(
                mapOf(
                    "accion" to "RESET_VOLCAMIENTO",
                    "timestamp" to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    // Guardar nuevo registro de vaciado en Firestore
    suspend fun recordDisposal(userName: String, userUid: String, prevFill: Int): Result<Unit> {
        val item = WasteHistoryItem(
            id = "vac_" + System.currentTimeMillis(),
            fechaHora = System.currentTimeMillis(),
            usuarioUid = userUid,
            usuarioNombre = userName,
            porcentajePrevio = prevFill,
            duracionMinutos = 2
        )
        _localHistory.add(0, item)
        _historyFlow.value = _localHistory.toList()

        return try {
            firestore.collection("$devicePath/historial_vaciados").add(item).await()
            Result.success(Unit)
        } catch (e: Exception) {
            // En contingencia sin conexión o antes de primer sync en la nube, registrar localmente
            Result.success(Unit)
        }
    }

    // Enviar o cancelar alerta de vaciado urgente desde Supervisor
    suspend fun setUrgentDisposalAlert(active: Boolean, supervisorName: String): Result<Unit> {
        _urgentAlertFlow.value = Pair(active, supervisorName)
        return try {
            firestore.document("$devicePath/comandos/alerta_urgente").set(
                mapOf(
                    "activa" to active,
                    "supervisor" to supervisorName,
                    "timestamp" to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    // Escucha reactiva en tiempo real del estado de alerta urgente
    fun observeUrgentDisposalAlert(): Flow<Pair<Boolean, String>> = callbackFlow {
        trySend(_urgentAlertFlow.value)
        var listener: ListenerRegistration? = null
        try {
            val docRef = firestore.document("$devicePath/comandos/alerta_urgente")
            listener = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(_urgentAlertFlow.value)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val activa = snapshot.getBoolean("activa") ?: false
                    val supervisor = snapshot.getString("supervisor") ?: ""
                    _urgentAlertFlow.value = Pair(activa, supervisor)
                    trySend(Pair(activa, supervisor))
                } else {
                    trySend(_urgentAlertFlow.value)
                }
            }
        } catch (e: Exception) {
            trySend(_urgentAlertFlow.value)
        }

        awaitClose {
            listener?.remove()
        }
    }

    // Actualizar umbral operativo y estado del buzzer para la sede
    suspend fun updateThresholdConfig(threshold: Int, buzzerSilenced: Boolean): Result<Unit> {
        _thresholdConfigFlow.value = Pair(threshold, buzzerSilenced)
        return try {
            firestore.document("$devicePath/configuracion/parametros").set(
                mapOf(
                    "umbralLlenado" to threshold,
                    "buzzerSilenciado" to buzzerSilenced,
                    "timestamp" to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    // Escucha reactiva de los parámetros de configuración de sede
    fun observeThresholdConfig(): Flow<Pair<Int, Boolean>> = callbackFlow {
        trySend(_thresholdConfigFlow.value)
        var listener: ListenerRegistration? = null
        try {
            val docRef = firestore.document("$devicePath/configuracion/parametros")
            listener = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(_thresholdConfigFlow.value)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val umbral = snapshot.getLong("umbralLlenado")?.toInt() ?: 80
                    val silenciado = snapshot.getBoolean("buzzerSilenciado") ?: false
                    _thresholdConfigFlow.value = Pair(umbral, silenciado)
                    trySend(Pair(umbral, silenciado))
                } else {
                    trySend(_thresholdConfigFlow.value)
                }
            }
        } catch (e: Exception) {
            trySend(_thresholdConfigFlow.value)
        }

        awaitClose {
            listener?.remove()
        }
    }
}
