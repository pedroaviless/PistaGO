package me.nacimiento.pistago.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.nacimiento.pistago.domain.model.ListaEspera
import me.nacimiento.pistago.domain.repository.ListaEsperaRepository
import javax.inject.Inject

data class ListaEsperaUiState(
    val isLoading: Boolean = false,
    val lista: List<ListaEspera> = emptyList(),
    val error: String? = null,
    val apuntadoOk: Boolean = false
)

@HiltViewModel
class ListaEsperaViewModel @Inject constructor(
    private val listaEsperaRepository: ListaEsperaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListaEsperaUiState())
    val uiState: StateFlow<ListaEsperaUiState> = _uiState

    fun getMiLista() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = listaEsperaRepository.getMiLista()
            result.fold(
                onSuccess = { _uiState.value = ListaEsperaUiState(lista = it) },
                onFailure = { _uiState.value = ListaEsperaUiState(error = it.message) }
            )
        }
    }

    fun apuntarse(pistaId: Long, fechaHora: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = listaEsperaRepository.apuntarse(pistaId, fechaHora)
            result.fold(
                onSuccess = {
                    getMiLista()
                    _uiState.value = _uiState.value.copy(apuntadoOk = true)
                },
                onFailure = { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
            )
        }
    }

    fun salir(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = listaEsperaRepository.salir(id)
            result.fold(
                onSuccess = { getMiLista() },
                onFailure = { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
            )
        }
    }
}