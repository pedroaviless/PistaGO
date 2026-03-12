package me.nacimiento.pistago.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.nacimiento.pistago.domain.model.Reserva
import me.nacimiento.pistago.domain.repository.ReservaRepository
import javax.inject.Inject

data class ReservaUiState(
    val isLoading: Boolean = false,
    val reservas: List<Reserva> = emptyList(),
    val reservaCreada: Reserva? = null,
    val error: String? = null,
    val horasOcupadas: List<String> = emptyList()
)

@HiltViewModel
class ReservaViewModel @Inject constructor(
    private val reservaRepository: ReservaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReservaUiState())
    val uiState: StateFlow<ReservaUiState> = _uiState

    fun getMisReservas() {
        viewModelScope.launch {
            _uiState.value = ReservaUiState(isLoading = true)
            val result = reservaRepository.getMisReservas()
            result.fold(
                onSuccess = { _uiState.value = ReservaUiState(reservas = it) },
                onFailure = { _uiState.value = ReservaUiState(error = it.message) }
            )
        }
    }

    fun crearReserva(pistaId: Long, fechaHora: String, duracionMin: Int = 60) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = reservaRepository.crearReserva(pistaId, fechaHora, duracionMin)
            result.fold(
                onSuccess = { _uiState.value = _uiState.value.copy(isLoading = false, reservaCreada = it) },
                onFailure = { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
            )
        }
    }

    fun cancelarReserva(id: Long) {
        viewModelScope.launch {
            val result = reservaRepository.cancelarReserva(id)
            result.fold(
                onSuccess = { getMisReservas() },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    fun getTodasLasReservas() {
        viewModelScope.launch {
            _uiState.value = ReservaUiState(isLoading = true)
            val result = reservaRepository.getTodasLasReservas()
            result.fold(
                onSuccess = { _uiState.value = ReservaUiState(reservas = it) },
                onFailure = { _uiState.value = ReservaUiState(error = it.message) }
            )
        }
    }

    fun getDisponibilidad(fecha: String) {
        viewModelScope.launch {
            val result = reservaRepository.getDisponibilidad(fecha)
            result.fold(
                onSuccess = { _uiState.value = _uiState.value.copy(horasOcupadas = it) },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
            )
        }
    }
}