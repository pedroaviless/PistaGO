package me.nacimiento.pistago.data.remote.dto

data class ListaEsperaRequest(
    val pistaId: Long,
    val fechaHora: String
)

data class ListaEsperaResponse(
    val id: Long,
    val pistaId: Long,
    val nombrePista: String,
    val fechaHora: String,
    val posicion: Int,
    val estado: String = "ACTIVO",
    val notificado: Boolean = false
)