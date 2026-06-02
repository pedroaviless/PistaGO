package me.nacimiento.pistago.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.nacimiento.pistago.domain.model.Estadisticas
import me.nacimiento.pistago.domain.repository.EstadisticasRepository
import javax.inject.Inject

data class EstadisticasUiState(
    val isLoading: Boolean = false,
    val datos: Estadisticas? = null,
    val error: String? = null
)

@HiltViewModel
class EstadisticasViewModel @Inject constructor(
    private val repository: EstadisticasRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EstadisticasUiState())
    val uiState: StateFlow<EstadisticasUiState> = _uiState

    fun cargar() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getEstadisticas().fold(
                onSuccess = { _uiState.value = EstadisticasUiState(datos = it) },
                onFailure = { _uiState.value = EstadisticasUiState(error = it.message) }
            )
        }
    }
}