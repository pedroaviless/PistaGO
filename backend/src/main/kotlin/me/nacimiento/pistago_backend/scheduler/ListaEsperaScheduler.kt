package me.nacimiento.pistago_backend.scheduler

import me.nacimiento.pistago_backend.service.ReservaService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * Tarea programada que cada 30 segundos rota las colas de espera:
 * elimina entradas cuyo plazo de confirmación (3 minutos) ha expirado
 * y notifica al siguiente de la cola.
 */
@Component
class ListaEsperaScheduler(
    private val reservaService: ReservaService
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
}