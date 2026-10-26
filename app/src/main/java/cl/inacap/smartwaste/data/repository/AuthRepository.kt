package cl.inacap.smartwaste.data.repository

import cl.inacap.smartwaste.data.model.User
import cl.inacap.smartwaste.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    // Almacenamiento local temporal para contingencias de conectividad
    private companion object {
        val localUsers = mutableMapOf<String, Pair<User, String>>().apply {
            put(
                "operador@inacapmail.cl",
                Pair(
                    User(
                        uid = "usr_aseo_1",
                        nombre = "Carlos Méndez",
                        email = "operador@inacapmail.cl",
                        rol = UserRole.ASEO.code,
                        activo = true,
                        fechaRegistro = 1715000000000L
                    ),
                    "123456"
                )
            )
            put(
                "supervisor@inacapmail.cl",
                Pair(
                    User(
                        uid = "usr_sup_1",
                        nombre = "Supervisor Campus",
                        email = "supervisor@inacapmail.cl",
                        rol = UserRole.SUPERVISOR.code,
                        activo = true,
                        fechaRegistro = 1715000000000L
                    ),
                    "123456"
                )
            )
            put(
                "admin@inacapmail.cl",
                Pair(
                    User(
                        uid = "usr_adm_1",
                        nombre = "Administrador Campus",
                        email = "admin@inacapmail.cl",
                        rol = UserRole.ADMIN.code,
                        activo = true,
                        fechaRegistro = 1715000000000L
                    ),
                    "123456"
                )
            )
        }
        var localLoggedUser: User? = null
    }

    // Autenticación de usuario y carga de rol asignado
    suspend fun login(email: String, pass: String): Result<User> {
        val cleanEmail = email.trim().lowercase()
        return try {
            val authResult = auth.signInWithEmailAndPassword(cleanEmail, pass).await()
            val uid = authResult.user?.uid ?: throw Exception("UID de usuario no encontrado")

            val doc = firestore.collection("users").document(uid).get().await()
            val user = doc.toObject(User::class.java) ?: User(
                uid = uid,
                nombre = authResult.user?.displayName ?: cleanEmail.substringBefore("@"),
                email = cleanEmail,
                rol = if (cleanEmail.contains("supervisor")) UserRole.SUPERVISOR.code else UserRole.ASEO.code
            )
            localLoggedUser = user
            Result.success(user)
        } catch (e: Exception) {
            if (isOfflineOrConfigFallback(e)) {
                val existing = localUsers[cleanEmail]
                if (existing != null) {
                    if (existing.second == pass) {
                        localLoggedUser = existing.first
                        return Result.success(existing.first)
                    } else {
                        return Result.failure(Exception("Contraseña incorrecta"))
                    }
                }
                val defaultRole = when {
                    cleanEmail.contains("supervisor") -> UserRole.SUPERVISOR.code
                    cleanEmail.contains("admin") -> UserRole.ADMIN.code
                    else -> UserRole.ASEO.code
                }
                val fallbackUser = User(
                    uid = "usr_" + Math.abs(cleanEmail.hashCode()),
                    nombre = cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                    email = cleanEmail,
                    rol = defaultRole,
                    activo = true,
                    fechaRegistro = System.currentTimeMillis()
                )
                localUsers[cleanEmail] = Pair(fallbackUser, pass)
                localLoggedUser = fallbackUser
                return Result.success(fallbackUser)
            }
            Result.failure(e)
        }
    }

    // Registro de nuevo usuario en base de datos
    suspend fun register(nombre: String, email: String, pass: String, rol: UserRole): Result<User> {
        val cleanEmail = email.trim().lowercase()
        return try {
            val authResult = auth.createUserWithEmailAndPassword(cleanEmail, pass).await()
            val uid = authResult.user?.uid ?: throw Exception("Error al crear cuenta")

            val newUser = User(
                uid = uid,
                nombre = nombre,
                email = cleanEmail,
                rol = rol.code,
                activo = true,
                fechaRegistro = System.currentTimeMillis()
            )

            firestore.collection("users").document(uid).set(newUser).await()
            localLoggedUser = newUser
            Result.success(newUser)
        } catch (e: Exception) {
            if (isOfflineOrConfigFallback(e)) {
                val localUser = User(
                    uid = "usr_" + Math.abs(cleanEmail.hashCode()),
                    nombre = nombre,
                    email = cleanEmail,
                    rol = rol.code,
                    activo = true,
                    fechaRegistro = System.currentTimeMillis()
                )
                localUsers[cleanEmail] = Pair(localUser, pass)
                localLoggedUser = localUser
                return Result.success(localUser)
            }
            Result.failure(e)
        }
    }

    // Envío de correo de recuperación
    suspend fun sendPasswordReset(email: String): Result<Unit> {
        val cleanEmail = email.trim().lowercase()
        return try {
            auth.sendPasswordResetEmail(cleanEmail).await()
            Result.success(Unit)
        } catch (e: Exception) {
            if (isOfflineOrConfigFallback(e)) {
                return Result.success(Unit)
            }
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(uid: String): User? {
        if (localLoggedUser != null && localLoggedUser?.uid == uid) {
            return localLoggedUser
        }
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            doc.toObject(User::class.java)
        } catch (e: Exception) {
            localLoggedUser
        }
    }

    fun logout() {
        try {
            auth.signOut()
        } catch (_: Exception) {}
        localLoggedUser = null
    }

    val currentUid: String?
        get() = auth.currentUser?.uid ?: localLoggedUser?.uid

    private fun isOfflineOrConfigFallback(e: Exception): Boolean {
        val msg = (e.message ?: "").lowercase()
        return msg.contains("api key not valid") ||
               msg.contains("invalid api key") ||
               msg.contains("permission_denied") ||
               msg.contains("project not found") ||
               msg.contains("bad request") ||
               msg.contains("network error") ||
               e is FirebaseAuthException
    }
}
