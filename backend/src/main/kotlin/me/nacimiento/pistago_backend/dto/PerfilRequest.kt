package me.nacimiento.pistago_backend.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class PerfilRequest(
    @field:NotBlank(message = "El nombre es obligatorio")
    @field:Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    val nombre: String,

    @field:Pattern(
        regexp = "^$|^[+]?[0-9]{9,15}$",
        message = "El teléfono debe tener entre 9 y 15 dígitos"
    )
    val telefono: String?
)

data class PasswordChangeRequest(
    @field:NotBlank(message = "La contraseña actual es obligatoria")
    val passwordActual: String,

    @field:NotBlank(message = "La nueva contraseña es obligatoria")
    @field:Size(min = 8, max = 100, message = "La nueva contraseña debe tener al menos 8 caracteres")
    val passwordNueva: String
)

data class PerfilResponse(
    val nombre: String,
    val email: String,
    val telefono: String?,
    val fotoUrl: String?,
    val rol: String
)