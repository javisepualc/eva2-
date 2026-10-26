package cl.inacap.smartwaste.data.model

data class WasteHistoryItem(
    val id: String = "",
    val fechaHora: Long = System.currentTimeMillis(),
    val usuarioUid: String = "",
    val usuarioNombre: String = "Auxiliar de Aseo",
    val porcentajePrevio: Int = 85,
    val duracionMinutos: Int = 3
)

data class DeviceConfig(
    val macAddress: String = "78:1C:3C:B7:72:70",
    val tiempoAperturaSegundos: Double = 4.0,
    val distanciaVacioCm: Double = 21.0,
    val distanciaLlenoCm: Double = 5.0,
    val brokerMqtt: String = "broker.hivemq.com",
    val topicoMqtt: String = "inacap/basurero/datos"
)
