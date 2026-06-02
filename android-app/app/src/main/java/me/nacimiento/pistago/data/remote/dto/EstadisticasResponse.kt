package me.nacimiento.pistago.data.remote.dto

data class EstadisticasResponse(
    val totalUsuarios: Long,
    val totalReservas: Long,
    val reservasConfirmadas: Long,
    val reservasCanceladas: Long,
    val reservasHoy: Long,
    val tasaCancelacion: Double,
    val topPistas: List<PistaStatDto>,
    val topUsuarios: List<UsuarioStatDto>,
    val reservasPorDiaSemana: List<DiaSemanaStatDto>
)

data class PistaStatDto(
    val nombrePista: String,
    val totalReservas: Long
)

data class UsuarioStatDto(
    val nombreUsuario: String,
    val totalReservas: Long
)

data class DiaSemanaStatDto(
    val dia: String,
    val total: Long
)