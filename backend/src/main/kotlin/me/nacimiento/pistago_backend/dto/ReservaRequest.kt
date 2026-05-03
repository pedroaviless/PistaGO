package me.nacimiento.pistago_backend.dto

import jakarta.validation.constraints.Future
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.time.LocalDateTime

data class ReservaRequest(
    @field:NotNull(message = "El ID de la pista es obligatorio")
    @field:Positive(message = "El ID de la pista debe ser positivo")
    val pistaId: Long,

    @field:NotNull(message = "La fecha y hora son obligatorias")
    @field:Future(message = "La fecha y hora deben ser futuras")
    val fechaHora: LocalDateTime,

    @field:Min(value = 30, message = "La duración mínima es 30 minutos")
    @field:Max(value = 240, message = "La duración máxima es 240 minutos (4 horas)")
    val duracionMin: Int = 60
)