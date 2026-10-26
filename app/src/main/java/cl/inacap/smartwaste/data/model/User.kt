package cl.inacap.smartwaste.data.model

enum class UserRole(val displayName: String, val code: String) {
    ASEO("Personal de Aseo", "aseo"),
    SUPERVISOR("Supervisor", "supervisor"),
    ADMIN("Administrador", "admin");

    companion object {
        fun fromCode(code: String?): UserRole {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: ASEO
        }
    }
}

data class User(
    val uid: String = "",
    val nombre: String = "",
    val email: String = "",
    val rol: String = UserRole.ASEO.code,
    val activo: Boolean = true,
    val fechaRegistro: Long = System.currentTimeMillis()
) {
    val userRole: UserRole
        get() = UserRole.fromCode(rol)
}
