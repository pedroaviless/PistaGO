package me.nacimiento.pistago_backend.service

import me.nacimiento.pistago_backend.domain.entity.Notificacion
import me.nacimiento.pistago_backend.domain.entity.Reserva
import me.nacimiento.pistago_backend.domain.enums.EstadoReserva
import me.nacimiento.pistago_backend.domain.enums.RolUsuario
import me.nacimiento.pistago_backend.domain.enums.TipoNotificacion
import me.nacimiento.pistago_backend.domain.repository.ListaEsperaRepository
import me.nacimiento.pistago_backend.domain.repository.NotificacionRepository
import me.nacimiento.pistago_backend.domain.repository.PistaRepository
import me.nacimiento.pistago_backend.domain.repository.ReservaRepository
import me.nacimiento.pistago_backend.domain.repository.UsuarioRepository
import me.nacimiento.pistago_backend.dto.ReservaRequest
import me.nacimiento.pistago_backend.dto.ReservaResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class ReservaService(
    private val reservaRepository: ReservaRepository,
    private val pistaRepository: PistaRepository,
    private val usuarioRepository: UsuarioRepository,
    private val listaEsperaRepository: ListaEsperaRepository,
    private val notificacionRepository: NotificacionRepository,
    private val fcmService: FcmService
) {
    private val log = LoggerFactory.getLogger(ReservaService::class.java)

    @Transactional
    fun crear(email: String, request: ReservaRequest): ReservaResponse {
        val usuario = usuarioRepository.findByEmail(email)
            ?: throw IllegalArgumentException("Usuario no encontrado")

        val pista = pistaRepository.findById(request.pistaId)
            .orElseThrow { IllegalArgumentException("Pista no encontrada") }

        if (!pista.activa) throw IllegalArgumentException("La pista no está activa")

        // Regla: un usuario USUARIO solo puede tener una reserva activa simultánea.
        // Admin y Superusuario quedan exentos.
        if (usuario.rol == RolUsuario.USUARIO) {
            val ahora = LocalDateTime.now()
            // Buscamos reservas confirmadas cuya hora de inicio sea desde hace una jornada
            // (suficiente margen: las reservas duran como máximo unas horas, no días)
            val desde = ahora.minusHours(24)
            val reservasRecientes = reservaRepository.findReservasConfirmadasDesde(usuario.id, desde)
            val tieneReservaActiva = reservasRecientes.any { reserva ->
                val finReserva = reserva.fechaHora.plusMinutes(reserva.duracionMin.toLong())
                finReserva.isAfter(ahora)
            }
            if (tieneReservaActiva) {
                throw IllegalArgumentException("Ya tienes una reserva activa. Espera a que termine para reservar otra.")
            }
        }

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

        if (reserva.usuario.id != usuario.id && usuario.rol != RolUsuario.ADMINISTRADOR) {
            throw IllegalArgumentException("No autorizado")
        }
        if (reserva.estado != EstadoReserva.CONFIRMADA) throw IllegalArgumentException("La reserva no se puede cancelar")

        val cancelada = reserva.copy(
            estado = EstadoReserva.CANCELADA,
            updatedAt = LocalDateTime.now()
        )
        val guardada = reservaRepository.save(cancelada)

        // Avisar al primero de la lista de espera de esa pista+franja (si hay)
        notificarPrimeroEnEspera(reserva)

        return guardada.toResponse()
    }

    /**
     * Cuando se cancela una reserva, busca al primero de la cola de espera
     * para esa pista y franja horaria que aún no haya sido notificado,
     * le envía una push y registra la notificación.
     */
    private fun notificarPrimeroEnEspera(reservaCancelada: Reserva) {
        val cola = listaEsperaRepository.findByPistaIdAndFechaHoraOrderByCreatedAtAsc(
            reservaCancelada.pista.id!!,
            reservaCancelada.fechaHora
        )

        // Primero de la cola que todavía NO ha sido notificado
        val primero = cola.firstOrNull { !it.notificado } ?: run {
            log.info("No hay nadie pendiente de notificar en lista de espera para pista=${reservaCancelada.pista.id} fecha=${reservaCancelada.fechaHora}")
            return
        }

        val destinatario = primero.usuario
        val nombrePista = reservaCancelada.pista.nombre
        val fechaFormateada = reservaCancelada.fechaHora.format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm")
        )

        val titulo = "¡Tu turno en $nombrePista!"
        val mensaje = "Se ha liberado una plaza en $nombrePista el $fechaFormateada. ¡Reserva ya antes de que otro la coja!"

        // Enviar push
        val enviada = fcmService.enviarNotificacion(
            fcmToken = destinatario.fcmToken,
            titulo = titulo,
            cuerpo = mensaje,
            datos = mapOf(
                "tipo" to TipoNotificacion.TURNO_ESPERA.name,
                "pistaId" to reservaCancelada.pista.id.toString(),
                "fechaHora" to reservaCancelada.fechaHora.toString()
            )
        )

        // Marcar a este usuario como notificado para no volver a avisarle
        listaEsperaRepository.save(
            primero.copy(
                notificado = true,
                expiraEn = LocalDateTime.now().plusMinutes(10)
            )
        )

        // Registrar la notificación en BD
        notificacionRepository.save(
            Notificacion(
                usuario = destinatario,
                tipo = TipoNotificacion.TURNO_ESPERA,
                titulo = titulo,
                mensaje = mensaje,
                leida = false,
                reserva = reservaCancelada,
                enviadaPush = enviada,
                createdAt = LocalDateTime.now()
            )
        )

        log.info("Notificación de turno de espera ${if (enviada) "enviada" else "registrada (push falló)"} a usuario=${destinatario.id}")
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

    @Transactional
    fun getHorasOcupadasPorPistaYFecha(fecha: String, pistaId: Long): List<String> {
        val inicio = java.time.LocalDate.parse(fecha).atStartOfDay()
        val fin = inicio.plusDays(1)
        return reservaRepository.findByFechaHoraBetween(inicio, fin)
            .filter { it.estado == EstadoReserva.CONFIRMADA && it.pista.id == pistaId }
            .map { it.fechaHora.toLocalTime().toString().substring(0, 5) }
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