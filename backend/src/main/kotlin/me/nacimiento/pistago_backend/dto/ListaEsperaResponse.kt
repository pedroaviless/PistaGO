package me.nacimiento.pistago_backend.dto

import java.time.LocalDateTime

data class ListaEsperaResponse(
    val id: Long,
    val pistaId: Long,
    val nombrePista: String,
    val usuarioId: Long,
    val nombreUsuario: String,
    val fechaHora: LocalDateTime,
    val posicion: Int,
    val notificado: Boolean
)