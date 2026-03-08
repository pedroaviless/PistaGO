package me.nacimiento.pistago.data.remote.dto

data class ReservaRequest(
    val pistaId: Long,
    val fechaHora: String,
    val duracionMin: Int = 60
)

data class ReservaResponse(
    val id: Long,
    val pistaId: Long,
    val nombrePista: String,
    val usuarioId: Long,
    val nombreUsuario: String,
    val fechaHora: String,
    val duracionMin: Int,
    val estado: String
)