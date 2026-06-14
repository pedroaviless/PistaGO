package me.nacimiento.pistago_backend.scheduler

import jakarta.transaction.Transactional
import me.nacimiento.pistago_backend.domain.repository.ListaEsperaRepository
import me.nacimiento.pistago_backend.service.ReservaService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * Tarea programada que cada 30 segundos rota las colas de espera:
 * elimina entradas cuyo plazo de confirmación (3 minutos) ha expirado
 * y notifica al siguiente de la cola.
 */
@Component
class ListaEsperaScheduler(
    private val reservaService: ReservaService,
    private val listaEsperaRepository: ListaEsperaRepository
) {
    private val log = LoggerFactory.getLogger(ListaEsperaScheduler::class.java)

    @Scheduled(fixedRate = 30_000)   // cada 30 segundos
    fun rotarColasExpiradas() {
        try {
            reservaService.procesarColasExpiradas()
        } catch (e: Exception) {
            log.error("Error al procesar colas expiradas: ${e.message}", e)
        }
    }
    /**
     * Limpieza periódica de entradas obsoletas en lista de espera.
     * Elimina cualquier entrada cuya franja temporal ya haya pasado,
     * incluidas las que no llegaron a notificarse porque la reserva
     * original nunca se canceló.
     * Se ejecuta una vez por hora.
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    fun limpiarEntradasObsoletas() {
        try {
            val ahora = LocalDateTime.now()
            val borradas = listaEsperaRepository.deleteByFechaHoraBefore(ahora)
            if (borradas > 0) {
                log.info("Limpieza lista de espera: $borradas entradas eliminadas por fecha pasada.")
            }
        } catch (e: Exception) {
            log.error("Error en limpieza de lista de espera: ${e.message}", e)
        }
    }
}