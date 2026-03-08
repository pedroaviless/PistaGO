package me.nacimiento.pistago.domain.model

data class Reserva(
    val id: Long,
    val pistaId: Long,
    val nombrePista: String,
    val usuarioId: Long,
    val nombreUsuario: String,
    val fechaHora: String,
    val duracionMin: Int,
    val estado: String
)