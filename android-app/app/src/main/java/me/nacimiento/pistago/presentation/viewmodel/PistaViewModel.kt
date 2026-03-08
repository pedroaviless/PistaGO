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
}