package me.nacimiento.pistago.domain.model

data class ListaEspera(
    val id: Long,
    val pistaId: Long,
    val nombrePista: String,
    val fechaHora: String,
    val posicion: Int,
    val estado: String
)