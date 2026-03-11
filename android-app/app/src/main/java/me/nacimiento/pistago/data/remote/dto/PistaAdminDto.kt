package me.nacimiento.pistago.data.remote.dto
import me.nacimiento.pistago.data.remote.dto.PistaAdminRequest

data class PistaAdminRequest(
    val nombre: String,
    val tipo: String,
    val descripcion: String? = null
)