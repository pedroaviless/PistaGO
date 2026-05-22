package me.nacimiento.pistago.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.nacimiento.pistago.data.local.TokenDataStore
import me.nacimiento.pistago.domain.model.Usuario
import me.nacimiento.pistago.domain.repository.AuthRepository
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val usuario: Usuario? = null,
    val error: String? = null,
    val telefono: String? = null,
    val fotoUrl: String? = null,
    val perfilActualizado: Boolean = false,
    val passwordActualizada: Boolean = false,
    val fotoSubida: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenDataStore: TokenDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    init {
        loadUsuario()
    }

    fun loadUsuario() {
        viewModelScope.launch {
            val result = authRepository.getUsuarioActual()
            result.fold(
                onSuccess = { if (it != null) _uiState.value = _uiState.value.copy(usuario = it) },
                onFailure = { /* sin sesión */ }
            )
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            authRepository.login(email, password).fold(
                onSuccess = {
                    _uiState.value = AuthUiState(usuario = it)
                    // Registrar el token FCM tras login exitoso
                    authRepository.registrarFcmToken()
                },
                onFailure = { _uiState.value = AuthUiState(error = it.message) }
            )
        }
    }

    fun register(nombre: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            authRepository.register(nombre, email, password).fold(
                onSuccess = {
                    _uiState.value = AuthUiState(usuario = it)
                    authRepository.registrarFcmToken()
                            },
                onFailure = { _uiState.value = AuthUiState(error = it.message) }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState()
        }
    }

    fun getPerfil() {
        viewModelScope.launch {
            authRepository.getPerfil().fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        telefono = it.telefono,
                        fotoUrl = it.fotoUrl
                    )
                },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
            )
        }
    }

    fun actualizarPerfil(nombre: String, telefono: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            authRepository.actualizarPerfil(nombre, telefono).fold(
                onSuccess = { perfil ->
                    // Actualizar también el nombre del Usuario en sesión
                    val usuarioActual = _uiState.value.usuario
                    val usuarioNuevo = usuarioActual?.copy(nombre = perfil.nombre)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        usuario = usuarioNuevo,
                        telefono = perfil.telefono,
                        fotoUrl = perfil.fotoUrl,
                        perfilActualizado = true,
                        error = null
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun cambiarPassword(passwordActual: String, passwordNueva: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            authRepository.cambiarPassword(passwordActual, passwordNueva).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        passwordActualizada = true,
                        error = null
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun subirFotoPerfil(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            authRepository.subirFotoPerfil(uri).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        fotoUrl = it.fotoUrl,
                        fotoSubida = true,
                        error = null
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun clearMensajes() {
        _uiState.value = _uiState.value.copy(
            perfilActualizado = false,
            passwordActualizada = false,
            fotoSubida = false,
            error = null
        )
    }
}