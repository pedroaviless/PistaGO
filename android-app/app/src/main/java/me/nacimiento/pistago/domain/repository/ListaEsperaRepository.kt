package me.nacimiento.pistago.domain.repository

import me.nacimiento.pistago.domain.model.ListaEspera

interface ListaEsperaRepository {
    suspend fun apuntarse(pistaId: Long, fechaHora: String): Result<ListaEspera>
    suspend fun getMiLista(): Result<List<ListaEspera>>
    suspend fun salir(id: Long): Result<Unit>
}