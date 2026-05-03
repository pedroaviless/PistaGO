package me.nacimiento.pistago_backend.dto

import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.time.LocalDateTime

data class ListaEsperaRequest(
    @field:NotNull(message = "El ID de la pista es obligatorio")
    @field:Positive(message = "El ID de la pista debe ser positivo")
    val pistaId: Long,

    @field:NotNull(message = "La fecha y hora son obligatorias")
    @field:Future(message = "La fecha y hora deben ser futuras")
    val fechaHora: LocalDateTime
)