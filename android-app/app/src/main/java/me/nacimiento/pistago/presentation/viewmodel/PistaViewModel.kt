package me.nacimiento.pistago.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.nacimiento.pistago.domain.model.Pista
import me.nacimiento.pistago.domain.repository.PistaRepository
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class PistaUiState(
    val isLoading: Boolean = false,
    val pistas: List<Pista> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class PistaViewModel @Inject constructor(
    private val pistaRepository: PistaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PistaUiState())
    val uiState: StateFlow<PistaUiState> = _uiState

    init {
        getPistas()
    }

    fun getPistas() {
        viewModelScope.launch {
            _uiState.value = PistaUiState(isLoading = true)
            val result = pistaRepository.getPistas()
            result.fold(
                onSuccess = { _uiState.value = PistaUiState(pistas = it) },
                onFailure = { _uiState.value = PistaUiState(error = it.message) }
            )
        }
    }

    fun loadTodasLasPistas() {
        viewModelScope.launch {
            _uiState.value = PistaUiState(isLoading = true)
            val result = pistaRepository.getTodasLasPistas()
            result.fold(
                onSuccess = { _uiState.value = PistaUiState(pistas = it) },
                onFailure = { _uiState.value = PistaUiState(error = it.message) }
            )
        }
    }

    fun crearPista(nombre: String, tipo: String, descripcion: String?) {
        viewModelScope.launch {
            val result = pistaRepository.crearPista(nombre, tipo, descripcion)
            result.fold(
                onSuccess = { loadTodasLasPistas() },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
            )
        }
    }

    fun actualizarPista(id: Long, nombre: String, tipo: String, descripcion: String?) {
        viewModelScope.launch {
            val result = pistaRepository.actualizarPista(id, nombre, tipo, descripcion)
            result.fold(
                onSuccess = { loadTodasLasPistas() },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
            )
        }
    }

    fun setActivaPista(id: Long, activa: Boolean) {
        viewModelScope.launch {
            val result = pistaRepository.setActivaPista(id, activa)
            result.fold(
                onSuccess = { loadTodasLasPistas() },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
            )
        }
    }

    var pistaSeleccionada by mutableStateOf<Pista?>(null)
        private set

    fun getPistaById(id: Long) {
        viewModelScope.launch {
            val result = pistaRepository.getPistaById(id)
            result.fold(
                onSuccess = { pistaSeleccionada = it },
                onFailure = { }
            )
        }
    }
}