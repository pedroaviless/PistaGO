package me.nacimiento.pistago_backend.dto

import java.time.LocalDateTime

data class ReservaRequest(
    val pistaId: Long,
    val fechaHora: LocalDateTime,
    val duracionMin: Int = 60
)