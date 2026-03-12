package me.nacimiento.pistago_backend.service

import me.nacimiento.pistago_backend.domain.entity.Reserva
import me.nacimiento.pistago_backend.domain.enums.EstadoReserva
import me.nacimiento.pistago_backend.domain.repository.PistaRepository
import me.nacimiento.pistago_backend.domain.repository.ReservaRepository
import me.nacimiento.pistago_backend.domain.repository.UsuarioRepository
import me.nacimiento.pistago_backend.dto.ReservaRequest
import me.nacimiento.pistago_backend.dto.ReservaResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ReservaService(
    private val reservaRepository: ReservaRepository,
    private val pistaRepository: PistaRepository,
    private val usuarioRepository: UsuarioRepository
) {

    @Transactional
    fun crear(email: String, request: ReservaRequest): ReservaResponse {
        val usuario = usuarioRepository.findByEmail(email)
            ?: throw IllegalArgumentException("Usuario no encontrado")

        val pista = pistaRepository.findById(request.pistaId)
            .orElseThrow { IllegalArgumentException("Pista no encontrada") }

        if (!pista.activa) throw IllegalArgumentException("La pista no está activa")

        val existente = reservaRepository.findReservaActiva(request.pistaId, request.fechaHora)
        if (existente != null) throw IllegalArgumentException("La pista ya está reservada en ese horario")

        val reserva = Reserva(
            usuario = usuario,
            pista = pista,
            fechaHora = request.fechaHora,
            duracionMin = request.duracionMin,
            estado = EstadoReserva.CONFIRMADA,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        return reservaRepository.save(reserva).toResponse()
    }

    @Transactional
    fun getMisReservas(email: String): List<ReservaResponse> {
        val usuario = usuarioRepository.findByEmail(email)
            ?: throw IllegalArgumentException("Usuario no encontrado")
        return reservaRepository.findByUsuarioId(usuario.id!!)
            .map { it.toResponse() }
    }

    @Transactional
    fun cancelar(email: String, reservaId: Long): ReservaResponse {
        val usuario = usuarioRepository.findByEmail(email)
            ?: throw IllegalArgumentException("Usuario no encontrado")

        val reserva = reservaRepository.findById(reservaId)
            .orElseThrow { IllegalArgumentException("Reserva no encontrada") }

        if (reserva.usuario.id != usuario.id) throw IllegalArgumentException("No autorizado")
        if (reserva.estado != EstadoReserva.CONFIRMADA) throw IllegalArgumentException("La reserva no se puede cancelar")

        val cancelada = reserva.copy(
            estado = EstadoReserva.CANCELADA,
            updatedAt = LocalDateTime.now()
        )
        return reservaRepository.save(cancelada).toResponse()
    }

    @Transactional
    fun getHorasOcupadasPorFecha(fecha: String): List<String> {
        val inicio = java.time.LocalDate.parse(fecha).atStartOfDay()
        val fin = inicio.plusDays(1)

        val reservasDelDia = reservaRepository.findByFechaHoraBetween(inicio, fin)
            .filter { it.estado == EstadoReserva.CONFIRMADA }

        val pistasActivasPorTipo = pistaRepository.findAll()
            .filter { it.activa }
            .groupBy { it.tipo }
            .mapValues { it.value.size }

        return reservasDelDia
            .groupBy { it.fechaHora.toLocalTime().toString().substring(0, 5) }
            .filter { (_, reservasHora) ->
                val reservasPorTipo = reservasHora.groupBy { it.pista.tipo }.mapValues { it.value.size }
                reservasPorTipo.all { (tipo, count) ->
                    val totalPistas = pistasActivasPorTipo[tipo] ?: 0
                    count >= totalPistas
                }
            }
            .keys.toList()

    }

    private fun Reserva.toResponse() = ReservaResponse(
        id = id!!,
        pistaId = pista.id!!,
        nombrePista = pista.nombre,
        usuarioId = usuario.id!!,
        nombreUsuario = usuario.nombre,
        fechaHora = fechaHora,
        duracionMin = duracionMin,
        estado = estado
    )

    @Transactional
    fun getTodas(): List<ReservaResponse> {
        return reservaRepository.findAll().map { it.toResponse() }
    }
}