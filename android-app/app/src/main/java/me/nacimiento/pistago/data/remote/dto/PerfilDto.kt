package me.nacimiento.pistago.data.remote.dto

// Para actualizar nombre y teléfono (sin foto, sin password)
data class PerfilRequest(
    val nombre: String,
    val telefono: String?
)

// Para cambio de contraseña (endpoint separado)
data class PasswordChangeRequest(
    val passwordActual: String,
    val passwordNueva: String
)

// Respuesta sin cambios
data class PerfilResponse(
    val nombre: String,
    val email: String,
    val telefono: String?,
    val fotoUrl: String?,
    val rol: String
)