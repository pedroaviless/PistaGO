package me.nacimiento.pistago.presentation.viewmodel

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
    val error: String? = null
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
                onSuccess = { if (it != null) _uiState.value = AuthUiState(usuario = it) },
                onFailure = { /* no hay sesión activa */ }
            )
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = authRepository.login(email, password)
            result.fold(
                onSuccess = { _uiState.value = AuthUiState(usuario = it) },
                onFailure = { _uiState.value = AuthUiState(error = it.message) }
            )
        }
    }

    fun register(nombre: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = authRepository.register(nombre, email, password)
            result.fold(
                onSuccess = { _uiState.value = AuthUiState(usuario = it) },
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
}