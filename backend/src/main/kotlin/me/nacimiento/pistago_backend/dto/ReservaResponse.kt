package me.nacimiento.pistago_backend.dto

import me.nacimiento.pistago_backend.domain.enums.EstadoReserva
import java.time.LocalDateTime

data class ReservaResponse(
    val id: Long,
    val pistaId: Long,
    val nombrePista: String,
    val usuarioId: Long,
    val nombreUsuario: String,
    val fechaHora: LocalDateTime,
    val duracionMin: Int,
    val estado: EstadoReserva
)