package me.nacimiento.pistago_backend.domain.repository

import me.nacimiento.pistago_backend.domain.entity.Reserva
import me.nacimiento.pistago_backend.domain.enums.EstadoReserva
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ReservaRepository : JpaRepository<Reserva, Long> {
    fun findByUsuarioId(usuarioId: Long): List<Reserva>
    fun findByPistaIdAndEstado(pistaId: Long, estado: EstadoReserva): List<Reserva>
    fun findByUsuarioIdAndEstado(usuarioId: Long, estado: EstadoReserva): List<Reserva>
    fun findByFechaHoraBetween(inicio: LocalDateTime, fin: LocalDateTime): List<Reserva>

    @Query("SELECT r FROM Reserva r WHERE r.pista.id = :pistaId AND r.fechaHora = :fechaHora AND r.estado = 'CONFIRMADA'")
    fun findReservaActiva(pistaId: Long, fechaHora: LocalDateTime): Reserva?

    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.usuario.id = :usuarioId AND r.fechaHora >= :inicioSemana AND r.fechaHora < :finSemana AND r.estado = 'CONFIRMADA'")
    fun countReservasSemana(usuarioId: Long, inicioSemana: LocalDateTime, finSemana: LocalDateTime): Long

    @Query("""
        SELECT r FROM Reserva r 
        WHERE r.usuario.id = :usuarioId 
          AND r.estado = 'CONFIRMADA' 
          AND r.fechaHora >= :desde
    """)
    fun findReservasConfirmadasDesde(usuarioId: Long, desde: LocalDateTime): List<Reserva>

}