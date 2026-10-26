package cl.inacap.smartwaste.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.inacap.smartwaste.data.model.User
import cl.inacap.smartwaste.data.model.UserRole
import cl.inacap.smartwaste.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val user: User, val message: String = "") : AuthUiState()
    data class PasswordResetSent(val email: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    var currentUser: User? = null
        private set

    init {
        // Comprobar si hay sesión guardada
        val uid = repository.currentUid
        if (uid != null) {
            viewModelScope.launch {
                val user = repository.getUserProfile(uid)
                if (user != null) {
                    currentUser = user
                    _uiState.value = AuthUiState.Success(user)
                }
            }
        }
    }

    // Inicio de sesión
    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = AuthUiState.Error("Ingresa tu correo y contraseña")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repository.login(email.trim(), pass)
            result.onSuccess { user ->
                currentUser = user
                _uiState.value = AuthUiState.Success(user)
            }.onFailure { error ->
                _uiState.value = AuthUiState.Error(
                    error.localizedMessage ?: "Error al iniciar sesión"
                )
            }
        }
    }

    // Registro de cuenta
    fun register(nombre: String, email: String, pass: String, passConfirm: String, rol: UserRole) {
        if (nombre.isBlank() || email.isBlank() || pass.isBlank()) {
            _uiState.value = AuthUiState.Error("Todos los campos son obligatorios")
            return
        }
        if (pass.length < 8) {
            _uiState.value = AuthUiState.Error("La contraseña debe tener al menos 8 caracteres")
            return
        }
        if (!pass.any { it.isUpperCase() }) {
            _uiState.value = AuthUiState.Error("La contraseña debe incluir al menos una letra mayúscula (A-Z)")
            return
        }
        if (!pass.any { it.isDigit() }) {
            _uiState.value = AuthUiState.Error("La contraseña debe incluir al menos un número (0-9)")
            return
        }
        if (!pass.any { !it.isLetterOrDigit() }) {
            _uiState.value = AuthUiState.Error("La contraseña debe incluir al menos un símbolo especial (!@#$... )")
            return
        }
        if (pass != passConfirm) {
            _uiState.value = AuthUiState.Error("Las contraseñas no coinciden")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repository.register(nombre.trim(), email.trim(), pass, rol)
            result.onSuccess { user ->
                currentUser = user
                _uiState.value = AuthUiState.Success(user, "¡Cuenta creada exitosamente!")
            }.onFailure { error ->
                _uiState.value = AuthUiState.Error(
                    error.localizedMessage ?: "Error al registrar usuario"
                )
            }
        }
    }

    // Enviar correo de restablecimiento
    fun resetPassword(email: String) {
        if (email.isBlank() || !email.contains("@")) {
            _uiState.value = AuthUiState.Error("Ingresa un correo institucional válido")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repository.sendPasswordReset(email.trim())
            result.onSuccess {
                _uiState.value = AuthUiState.PasswordResetSent(email.trim())
            }.onFailure { error ->
                _uiState.value = AuthUiState.Error(
                    error.localizedMessage ?: "No se pudo enviar el correo de recuperación"
                )
            }
        }
    }

    fun clearState() {
        _uiState.value = AuthUiState.Idle
    }

    fun logout() {
        repository.logout()
        currentUser = null
        _uiState.value = AuthUiState.Idle
    }
}
