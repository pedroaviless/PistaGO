package me.nacimiento.pistago_backend.dto

// Para actualizar nombre y teléfono (solo)
data class PerfilRequest(
    val nombre: String,
    val telefono: String?
)

// Para cambio de contraseña, separado
data class PasswordChangeRequest(
    val passwordActual: String,
    val passwordNueva: String
)

data class PerfilResponse(
    val nombre: String,
    val email: String,
    val telefono: String?,
    val fotoUrl: String?,
    val rol: String
)