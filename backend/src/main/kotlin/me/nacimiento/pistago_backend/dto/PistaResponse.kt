package me.nacimiento.pistago_backend.dto

import me.nacimiento.pistago_backend.domain.enums.TipoPista

data class PistaResponse(
    val id: Long,
    val nombre: String,
    val tipo: TipoPista,
    val descripcion: String?,
    val activa: Boolean
)