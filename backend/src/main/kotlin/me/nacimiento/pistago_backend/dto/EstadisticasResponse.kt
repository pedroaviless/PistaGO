package me.nacimiento.pistago_backend.dto

data class EstadisticasResponse(
    val totalUsuarios: Long,
    val totalReservas: Long,
    val reservasConfirmadas: Long,
    val reservasCanceladas: Long,
    val reservasExpiradas: Long,
    val reservasHoy: Long,
    val tasaCancelacion: Double,
    val topPistas: List<PistaStat>,
    val topUsuarios: List<UsuarioStat>,
    val reservasPorDiaSemana: List<DiaSemanaStat>
)
data class PistaStat(
    val nombrePista: String,
    val totalReservas: Long
)

data class UsuarioStat(
    val nombreUsuario: String,
    val totalReservas: Long
)

data class DiaSemanaStat(
    val dia: String,
    val total: Long
)