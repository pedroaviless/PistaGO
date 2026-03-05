package me.nacimiento.pistago_backend.dto

import me.nacimiento.pistago_backend.domain.enums.TipoPista

data class PistaRequest(
    val nombre: String,
    val tipo: TipoPista,
    val descripcion: String? = null
)