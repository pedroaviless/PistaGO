package me.nacimiento.pistago_backend.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank(message = "El email es obligatorio")
    @field:Email(message = "El formato del email no es válido")
    val email: String,

    @field:NotBlank(message = "La contraseña es obligatoria")
    val password: String
)