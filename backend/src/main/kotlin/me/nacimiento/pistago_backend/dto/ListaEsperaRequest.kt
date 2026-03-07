package me.nacimiento.pistago_backend.dto

import java.time.LocalDateTime

data class ListaEsperaRequest(
    val pistaId: Long,
    val fechaHora: LocalDateTime
)