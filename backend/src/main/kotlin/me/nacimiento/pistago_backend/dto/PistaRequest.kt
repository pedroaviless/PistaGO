package me.nacimiento.pistago_backend.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import me.nacimiento.pistago_backend.domain.enums.TipoPista

data class PistaRequest(
    @field:NotBlank(message = "El nombre es obligatorio")
    @field:Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    val nombre: String,

    @field:NotNull(message = "El tipo de pista es obligatorio")
    val tipo: TipoPista,

    @field:Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    val descripcion: String? = null
)