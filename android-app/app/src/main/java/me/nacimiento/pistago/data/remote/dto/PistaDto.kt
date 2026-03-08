package me.nacimiento.pistago.data.remote.dto

data class PistaResponse(
    val id: Long,
    val nombre: String,
    val tipo: String,
    val descripcion: String?,
    val activa: Boolean
)