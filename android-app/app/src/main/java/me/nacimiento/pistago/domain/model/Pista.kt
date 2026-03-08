package me.nacimiento.pistago.domain.model

data class Pista(
    val id: Long,
    val nombre: String,
    val tipo: String,
    val descripcion: String?,
    val activa: Boolean
)