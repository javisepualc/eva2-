package cl.inacap.smartwaste.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.inacap.smartwaste.data.model.TelemetryData
import cl.inacap.smartwaste.data.model.WasteHistoryItem
import cl.inacap.smartwaste.data.repository.WasteBinRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class ConnectionType {
    CLOUD, DIRECT_WIFI, SIMULATION
}

class WasteBinViewModel(
    private val repository: WasteBinRepository = WasteBinRepository()
) : ViewModel() {

    private val _telemetry = MutableStateFlow(TelemetryData(timestamp = 0L))
    val telemetry: StateFlow<TelemetryData> = _telemetry.asStateFlow()

    private val _history = MutableStateFlow<List<WasteHistoryItem>>(emptyList())
    val history: StateFlow<List<WasteHistoryItem>> = _history.asStateFlow()

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    private val _isSendingCommand = MutableStateFlow(false)
    val isSendingCommand: StateFlow<Boolean> = _isSendingCommand.asStateFlow()

    private val _connectionType = MutableStateFlow(ConnectionType.CLOUD)
    val connectionType: StateFlow<ConnectionType> = _connectionType.asStateFlow()

    private val _esp32Ip = MutableStateFlow("192.168.4.1")
    val esp32Ip: StateFlow<String> = _esp32Ip.asStateFlow()

    // Estado de Supervisión y Parámetros de Campus
    private val _alertaUrgente = MutableStateFlow(false)
    val alertaUrgente: StateFlow<Boolean> = _alertaUrgente.asStateFlow()

    private val _alertaSupervisor = MutableStateFlow("")
    val alertaSupervisor: StateFlow<String> = _alertaSupervisor.asStateFlow()

    private val _umbralAlerta = MutableStateFlow(80)
    val umbralAlerta: StateFlow<Int> = _umbralAlerta.asStateFlow()

    private val _buzzerSilenciado = MutableStateFlow(false)
    val buzzerSilenciado: StateFlow<Boolean> = _buzzerSilenciado.asStateFlow()

    private var pollingJob: Job? = null
    private var cloudJob: Job? = null
    private var simulationJob: Job? = null

    init {
        // Conexión oficial al Robot Físico vía Cloud Firestore y Gateway Wi-Fi
        startCloudListener()

        viewModelScope.launch {
            repository.observeHistory().collect { list ->
                if (_connectionType.value != ConnectionType.SIMULATION && list.isNotEmpty()) {
                    _history.value = list
                }
            }
        }

        viewModelScope.launch {
            repository.observeUrgentDisposalAlert().collect { (activa, supervisor) ->
                if (_connectionType.value != ConnectionType.SIMULATION || activa) {
                    _alertaUrgente.value = activa
                    _alertaSupervisor.value = supervisor
                }
            }
        }

        viewModelScope.launch {
            repository.observeThresholdConfig().collect { (umbral, silenciado) ->
                _umbralAlerta.value = umbral
                _buzzerSilenciado.value = silenciado
            }
        }
    }

    // Modo Demostración con Telemetría Dinámica y Creíble
    fun startSimulation() {
        stopAllJobs()
        _connectionType.value = ConnectionType.SIMULATION
        _feedbackMessage.value = "Modo Demostración activo: Telemetría simulada en tiempo real"

        // Datos históricos de muestra para trazabilidad y KPIs
        val now = System.currentTimeMillis()
        _history.value = listOf(
            WasteHistoryItem(
                id = "sim_1",
                fechaHora = now - 35 * 60 * 1000L,
                usuarioNombre = "Carlos Méndez",
                usuarioUid = "usr_aseo_1",
                porcentajePrevio = 84,
                duracionMinutos = 2
            ),
            WasteHistoryItem(
                id = "sim_2",
                fechaHora = now - 180 * 60 * 1000L,
                usuarioNombre = "Javiera Sepúlveda",
                usuarioUid = "usr_aseo_2",
                porcentajePrevio = 78,
                duracionMinutos = 2
            ),
            WasteHistoryItem(
                id = "sim_3",
                fechaHora = now - 24 * 3600 * 1000L - 10 * 60 * 1000L,
                usuarioNombre = "Rodrigo Morales",
                usuarioUid = "usr_adm_1",
                porcentajePrevio = 92,
                duracionMinutos = 3
            ),
            WasteHistoryItem(
                id = "sim_4",
                fechaHora = now - 28 * 3600 * 1000L,
                usuarioNombre = "Fernanda Contreras",
                usuarioUid = "usr_aseo_3",
                porcentajePrevio = 68,
                duracionMinutos = 2
            )
        )

        simulationJob = viewModelScope.launch {
            var step = 0
            while (isActive) {
                val fill = 74 + (if (step % 2 == 0) 1 else -1)
                val temp = 22.4 + (step % 3) * 0.2
                val hum = 56.0 + (step % 2) * 1.5
                val gas = 136 + (step % 4) * 2

                _telemetry.value = _telemetry.value.copy(
                    llenadoPorcentaje = fill,
                    distanciaCm = 6.2,
                    temperatura = Math.round(temp * 10.0) / 10.0,
                    humedad = Math.round(hum * 10.0) / 10.0,
                    gasADC = gas,
                    lineaBaseGas = 110,
                    volcado = false,
                    presenciaDetectada = (step % 3 != 0),
                    timestamp = System.currentTimeMillis()
                )
                delay(1500)
                step++
            }
        }
    }

    private fun startCloudListener() {
        stopAllJobs()
        _connectionType.value = ConnectionType.CLOUD
        cloudJob = viewModelScope.launch {
            repository.observeTelemetry().collect { data ->
                if (_connectionType.value == ConnectionType.CLOUD) {
                    _telemetry.value = data
                }
            }
        }
    }

    private fun stopAllJobs() {
        cloudJob?.cancel()
        pollingJob?.cancel()
        simulationJob?.cancel()
    }

    // Conexión por IP local (HTTP)
    fun connectDirectWifi(ip: String) {
        val cleanIp = ip.trim().removePrefix("http://").removeSuffix("/")
        _esp32Ip.value = cleanIp
        stopAllJobs()
        _connectionType.value = ConnectionType.DIRECT_WIFI
        _feedbackMessage.value = "Conectando al ESP32 en http://$cleanIp..."

        pollingJob = viewModelScope.launch {
            var consecutiveFailures = 0
            while (isActive) {
                val result = repository.fetchDirectTelemetry(cleanIp)
                result.onSuccess { data ->
                    consecutiveFailures = 0
                    _telemetry.value = data
                }.onFailure {
                    consecutiveFailures++
                    if (consecutiveFailures >= 3) {
                        _telemetry.value = TelemetryData(timestamp = 0L)
                    }
                }
                delay(1000)
            }
        }
    }

    // Cambiar a escucha en Firestore
    fun switchToCloud() {
        startCloudListener()
        _feedbackMessage.value = "Modo Nube activado: Sincronizando con Firestore..."
    }

    // Comando para abrir la tapa
    fun triggerLidOpening(seconds: Double = 4.0) {
        viewModelScope.launch {
            _isSendingCommand.value = true
            _feedbackMessage.value = "Enviando orden de apertura al ESP32..."

            when (_connectionType.value) {
                ConnectionType.SIMULATION -> {
                    delay(400)
                    _isSendingCommand.value = false
                    _feedbackMessage.value = "Tapa abriendo a 90° (${seconds.toInt()}s)"
                    _telemetry.value = _telemetry.value.copy(tapaAbierta = true)
                    viewModelScope.launch {
                        delay((seconds * 1000).toLong())
                        _telemetry.value = _telemetry.value.copy(tapaAbierta = false)
                        _feedbackMessage.value = "Tapa cerrada suavemente a 0°"
                    }
                }
                ConnectionType.DIRECT_WIFI -> {
                    val result = repository.sendDirectCommand(_esp32Ip.value, "abrir", "{\"accion\":\"ABRIR_TAPA\"}")
                    _isSendingCommand.value = false
                    result.onSuccess {
                        _feedbackMessage.value = "Tapa abriendo a 90° (${seconds.toInt()}s)"
                        _telemetry.value = _telemetry.value.copy(tapaAbierta = true)
                    }.onFailure {
                        _feedbackMessage.value = "Error al comunicar con ESP32 en http://${_esp32Ip.value}"
                    }
                }
                ConnectionType.CLOUD -> {
                    val result = repository.sendOpenLidCommand(seconds)
                    _isSendingCommand.value = false
                    result.onSuccess {
                        _feedbackMessage.value = "Orden enviada: Tapa abriendo (${seconds.toInt()}s)"
                    }.onFailure {
                        _feedbackMessage.value = "Error al enviar comando"
                    }
                }
            }
        }
    }

    // Activar o desactivar modo limpieza
    fun toggleCleaningMode(enabled: Boolean) {
        viewModelScope.launch {
            _isSendingCommand.value = true
            when (_connectionType.value) {
                ConnectionType.SIMULATION -> {
                    delay(300)
                    _isSendingCommand.value = false
                    _telemetry.value = _telemetry.value.copy(modoLimpieza = enabled, tapaAbierta = enabled)
                    _feedbackMessage.value = if (enabled) "Modo Limpieza activado (Tapa a 90°)" else "Modo Limpieza finalizado"
                }
                ConnectionType.DIRECT_WIFI -> {
                    val endpoint = if (enabled) "limpieza/activar" else "limpieza/desactivar"
                    repository.sendDirectCommand(_esp32Ip.value, endpoint)
                    _isSendingCommand.value = false
                    _telemetry.value = _telemetry.value.copy(modoLimpieza = enabled, tapaAbierta = enabled)
                    _feedbackMessage.value = if (enabled) "Modo Limpieza activado" else "Modo Limpieza finalizado"
                }
                ConnectionType.CLOUD -> {
                    val result = repository.setCleaningMode(enabled)
                    _isSendingCommand.value = false
                    result.onSuccess {
                        _feedbackMessage.value = if (enabled) "Modo Limpieza activado en Cloud" else "Modo Limpieza finalizado"
                    }.onFailure {
                        _feedbackMessage.value = "Error al actualizar Modo Limpieza"
                    }
                }
            }
        }
    }

    // Prueba del zumbador
    fun testBuzzer() {
        viewModelScope.launch {
            _isSendingCommand.value = true
            _feedbackMessage.value = "Enviando señal al buzzer..."
            when (_connectionType.value) {
                ConnectionType.SIMULATION -> {
                    delay(250)
                    _isSendingCommand.value = false
                    _feedbackMessage.value = "Sonido emitido en el zumbador (GPIO 33)."
                }
                ConnectionType.DIRECT_WIFI -> {
                    repository.sendDirectCommand(_esp32Ip.value, "buzzer")
                    _isSendingCommand.value = false
                    _feedbackMessage.value = "Sonido emitido en el ESP32."
                }
                ConnectionType.CLOUD -> {
                    val result = repository.sendBuzzerTest()
                    _isSendingCommand.value = false
                    result.onSuccess {
                        _feedbackMessage.value = "Sonido emitido en el ESP32."
                    }.onFailure {
                        _feedbackMessage.value = "No se pudo comunicar con el buzzer."
                    }
                }
            }
        }
    }

    // Conmutar sensor de proximidad
    fun toggleProximitySensor(enabled: Boolean) {
        viewModelScope.launch {
            when (_connectionType.value) {
                ConnectionType.SIMULATION -> {
                    delay(200)
                    _telemetry.value = _telemetry.value.copy(presenciaDetectada = enabled)
                    _feedbackMessage.value = if (enabled) "Sensor IR activado" else "Sensor IR desactivado"
                }
                ConnectionType.DIRECT_WIFI -> {
                    val endpoint = if (enabled) "sensores/ir/activar" else "sensores/ir/desactivar"
                    repository.sendDirectCommand(_esp32Ip.value, endpoint)
                    _feedbackMessage.value = if (enabled) "Sensor IR activado" else "Sensor IR desactivado"
                }
                ConnectionType.CLOUD -> {
                    repository.setProximitySensor(enabled)
                    _feedbackMessage.value = if (enabled) "Sensor IR activado (Cloud)" else "Sensor IR desactivado"
                }
            }
        }
    }

    // Restablecer alarma de inclinación
    fun confirmReposition() {
        viewModelScope.launch {
            when (_connectionType.value) {
                ConnectionType.SIMULATION -> {
                    delay(200)
                    _telemetry.value = _telemetry.value.copy(volcado = false)
                    _feedbackMessage.value = "Alarma de inclinación restablecida."
                }
                ConnectionType.DIRECT_WIFI -> {
                    repository.sendDirectCommand(_esp32Ip.value, "reset_volcado")
                    _telemetry.value = _telemetry.value.copy(volcado = false)
                    _feedbackMessage.value = "Alarma de inclinación restablecida."
                }
                ConnectionType.CLOUD -> {
                    repository.resetTiltAlarm()
                    _telemetry.value = _telemetry.value.copy(volcado = false)
                    _feedbackMessage.value = "Alarma de inclinación restablecida."
                }
            }
        }
    }

    // Registrar vaciado en Firestore
    fun registerDisposal(userName: String, userUid: String) {
        viewModelScope.launch {
            _isSendingCommand.value = true
            val prevFill = _telemetry.value.llenadoPorcentaje

            if (_connectionType.value == ConnectionType.SIMULATION) {
                delay(350)
                _isSendingCommand.value = false
                val newItem = WasteHistoryItem(
                    id = "sim_" + System.currentTimeMillis(),
                    fechaHora = System.currentTimeMillis(),
                    usuarioNombre = userName,
                    usuarioUid = userUid,
                    porcentajePrevio = prevFill,
                    duracionMinutos = 2
                )
                _history.value = listOf(newItem) + _history.value
                _telemetry.value = _telemetry.value.copy(llenadoPorcentaje = 0, distanciaCm = 24.0)

                if (_alertaUrgente.value) {
                    cancelarAlertaUrgente()
                    _feedbackMessage.value = "Vaciado registrado. Alerta prioritaria resuelta ($prevFill%)."
                } else {
                    _feedbackMessage.value = "Vaciado registrado correctamente ($prevFill%)."
                }
                return@launch
            }

            val result = repository.recordDisposal(userName, userUid, prevFill)
            _isSendingCommand.value = false

            result.onSuccess {
                if (_alertaUrgente.value) {
                    cancelarAlertaUrgente()
                    _feedbackMessage.value = "Vaciado registrado. Alerta prioritaria resuelta ($prevFill%)."
                } else {
                    _feedbackMessage.value = "Vaciado registrado correctamente ($prevFill%)."
                }
            }.onFailure {
                _feedbackMessage.value = "Error al registrar vaciado."
            }
        }
    }

    // Funciones de Supervisión
    fun solicitarVaciadoUrgente(supervisorNombre: String) {
        viewModelScope.launch {
            _alertaUrgente.value = true
            _alertaSupervisor.value = supervisorNombre
            _feedbackMessage.value = "Alerta prioritaria despachada a personal de aseo."
            repository.setUrgentDisposalAlert(true, supervisorNombre)
        }
    }

    fun cancelarAlertaUrgente() {
        viewModelScope.launch {
            _alertaUrgente.value = false
            _alertaSupervisor.value = ""
            _feedbackMessage.value = "Alerta de vaciado prioritario cancelada."
            repository.setUrgentDisposalAlert(false, "")
        }
    }

    fun setUmbralAlerta(nuevoUmbral: Int) {
        _umbralAlerta.value = nuevoUmbral
    }

    fun toggleBuzzerSede(silenciado: Boolean) {
        _buzzerSilenciado.value = silenciado
    }

    fun guardarParametrosSupervisor() {
        viewModelScope.launch {
            _feedbackMessage.value = "Guardando parámetros de sede en Firestore..."
            val result = repository.updateThresholdConfig(_umbralAlerta.value, _buzzerSilenciado.value)
            result.onSuccess {
                _feedbackMessage.value = "Parámetros de sede guardados en la nube."
            }.onFailure {
                _feedbackMessage.value = "Parámetros guardados localmente."
            }
        }
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        stopAllJobs()
    }
}
