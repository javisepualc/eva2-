package cl.inacap.smartwaste.data.model

data class TelemetryData(
    val llenadoPorcentaje: Int = 0,
    val distanciaCm: Double = 0.0,
    val temperatura: Double = 0.0,
    val humedad: Double = 0.0,
    val gasADC: Int = 0,
    val lineaBaseGas: Int = 0,
    val volcado: Boolean = false,
    val tapaAbierta: Boolean = false,
    val presenciaDetectada: Boolean = false,
    val modoLimpieza: Boolean = false,
    val timestamp: Long = 0L
) {
    // Control de enlace según eva1.docx: Caída si no hay datos en más de 6 segundos
    val isHardwareConnected: Boolean
        get() {
            if (timestamp == 0L) return false
            val diffSeconds = (System.currentTimeMillis() - timestamp) / 1000
            return diffSeconds <= 6
        }

    // Clasificación de Contenido idéntica a Nodo 2 (TFT)
    val estadoContenido: String
        get() = when {
            !isHardwareConnected -> "En espera de telemetría"
            humedad > 75.0 && temperatura > 24.0 -> "En descomposición"
            humedad > 62.0 -> "Húmedo"
            else -> "Seco"
        }

    // Clasificación de Aire Interior idéntica a Nodo 2 (TFT)
    val estadoAire: String
        get() = when {
            !isHardwareConnected -> "En espera de telemetría"
            lineaBaseGas == 0 -> "Calibrando sensor..."
            gasADC > lineaBaseGas * 20.0 -> "Gas peligroso"
            gasADC > lineaBaseGas * 1.20 -> "Olores detectados"
            else -> "Aire limpio"
        }

    // Semáforo de Acción Sugerida idéntico a Nodo 2 (TFT)
    val recomendacion: Pair<String, String>
        get() = when {
            !isHardwareConnected -> Pair("En espera de enlace", "OFFLINE")
            volcado -> Pair("Contenedor volcado", "DANGER")
            llenadoPorcentaje > 85 || (lineaBaseGas > 0 && gasADC > lineaBaseGas * 20.0) -> Pair("Requiere vaciado", "DANGER")
            llenadoPorcentaje > 60 || (humedad > 75.0 && temperatura > 24.0) -> Pair("Vaciar pronto", "WARN")
            else -> Pair("Operación normal", "OK")
        }
}
