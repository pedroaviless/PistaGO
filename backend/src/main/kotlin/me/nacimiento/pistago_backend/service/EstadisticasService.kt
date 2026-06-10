package me.nacimiento.pistago_backend.service

import me.nacimiento.pistago_backend.domain.enums.EstadoReserva
import me.nacimiento.pistago_backend.domain.repository.ReservaRepository
import me.nacimiento.pistago_backend.domain.repository.UsuarioRepository
import me.nacimiento.pistago_backend.dto.DiaSemanaStat
import me.nacimiento.pistago_backend.dto.EstadisticasResponse
import me.nacimiento.pistago_backend.dto.PistaStat
import me.nacimiento.pistago_backend.dto.UsuarioStat
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class EstadisticasService(
    private val reservaRepository: ReservaRepository,
    private val usuarioRepository: UsuarioRepository
) {
    fun getEstadisticas(): EstadisticasResponse {
        val totalUsuarios = usuarioRepository.count()
        val confirmadas = reservaRepository.countByEstado(EstadoReserva.CONFIRMADA)
        val canceladas = reservaRepository.countByEstado(EstadoReserva.CANCELADA)
        val expiradas = reservaRepository.countByEstado(EstadoReserva.EXPIRADA)
        val total = confirmadas + canceladas + expiradas

        val inicioHoy = LocalDate.now().atStartOfDay()
        val finHoy = inicioHoy.plusDays(1)
        val reservasHoy = reservaRepository.countReservasDia(inicioHoy, finHoy)

        val tasaCancelacion = if (total > 0) {
            (canceladas.toDouble() / total.toDouble()) * 100.0
        } else 0.0

        val topPistas = reservaRepository.topPistas()
            .take(3)
            .map { row -> PistaStat(row[0] as String, (row[1] as Number).toLong()) }

        val topUsuarios = reservaRepository.topUsuarios()
            .take(3)
            .map { row -> UsuarioStat(row[0] as String, (row[1] as Number).toLong()) }

        // PostgreSQL: 0=domingo, 1=lunes, ..., 6=sábado
        val nombresDias = mapOf(
            0 to "Domingo",
            1 to "Lunes",
            2 to "Martes",
            3 to "Miércoles",
            4 to "Jueves",
            5 to "Viernes",
            6 to "Sábado"
        )
        val reservasPorDia = reservaRepository.reservasPorDiaSemana().map { row ->
            val dow = (row[0] as Number).toInt()
            val count = (row[1] as Number).toLong()
            DiaSemanaStat(dia = nombresDias[dow] ?: "?", total = count)
        }

        return EstadisticasResponse(
            totalUsuarios = totalUsuarios,
            totalReservas = total,
            reservasConfirmadas = confirmadas,
            reservasCanceladas = canceladas,
            reservasHoy = reservasHoy,
            tasaCancelacion = String.format("%.1f", tasaCancelacion).replace(",", ".").toDouble(),
            topPistas = topPistas,
            topUsuarios = topUsuarios,
            reservasPorDiaSemana = reservasPorDia,
            reservasExpiradas = expiradas
        )
    }
}