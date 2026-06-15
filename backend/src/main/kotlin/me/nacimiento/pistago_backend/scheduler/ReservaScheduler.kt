package me.nacimiento.pistago_backend.scheduler

import me.nacimiento.pistago_backend.domain.enums.EstadoReserva
import me.nacimiento.pistago_backend.domain.repository.ReservaRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class ReservaScheduler(
    private val reservaRepository: ReservaRepository
) {
    private val log = LoggerFactory.getLogger(ReservaScheduler::class.java)

    /**
     * Transiciona las reservas CONFIRMADAS cuya hora de fin
     * (fecha_hora + duracion_min) ya ha pasado al estado EXPIRADA.
     * Mantiene la coherencia del ciclo de vida del modelo de reservas.
     * Se ejecuta una vez por hora.
     */
    @Scheduled(cron = "0 30 * * * *")
    @Transactional
    fun marcarReservasExpiradas() {
        try {
            val ahora = LocalDateTime.now()
            val actualizadas = reservaRepository.marcarReservasExpiradas(
                nuevoEstado = EstadoReserva.EXPIRADA.name,
                estadoActual = EstadoReserva.CONFIRMADA.name,
                ahora = ahora
            )
            if (actualizadas > 0) {
                log.info("Transición de reservas: $actualizadas marcadas como EXPIRADAS.")
            }
        } catch (e: Exception) {
            log.error("Error marcando reservas expiradas: ${e.message}", e)
        }
    }
}