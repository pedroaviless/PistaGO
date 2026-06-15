package me.nacimiento.pistago_backend.domain.repository

import me.nacimiento.pistago_backend.domain.entity.Reserva
import me.nacimiento.pistago_backend.domain.enums.EstadoReserva
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
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

    // ===== Queries para estadísticas =====

    fun countByEstado(estado: EstadoReserva): Long

    @Query("""
        SELECT COUNT(r) FROM Reserva r 
        WHERE r.fechaHora >= :inicioDia AND r.fechaHora < :finDia
    """)
    fun countReservasDia(inicioDia: LocalDateTime, finDia: LocalDateTime): Long

    @Query("""
        SELECT p.nombre, COUNT(r) 
        FROM Reserva r JOIN r.pista p 
        WHERE r.estado IN ('CONFIRMADA', 'EXPIRADA')
        GROUP BY p.id, p.nombre 
        ORDER BY COUNT(r) DESC
    """)
    fun topPistas(): List<Array<Any>>

    @Query("""
        SELECT u.nombre, COUNT(r) 
        FROM Reserva r JOIN r.usuario u 
        WHERE r.estado IN ('CONFIRMADA', 'EXPIRADA')
        GROUP BY u.id, u.nombre 
        ORDER BY COUNT(r) DESC
    """)
    fun topUsuarios(): List<Array<Any>>

    @Query(
        value = """
            SELECT EXTRACT(DOW FROM fecha_hora)::int AS dow, COUNT(*) 
            FROM reservas 
            WHERE estado IN ('CONFIRMADA', 'EXPIRADA')
            GROUP BY dow
            ORDER BY dow
        """,
        nativeQuery = true
    )
    fun reservasPorDiaSemana(): List<Array<Any>>

    // ===== Transición automática a EXPIRADA =====

    @Modifying
    @Query(
        value = """
            UPDATE reservas 
            SET estado = :nuevoEstado, updated_at = NOW() 
            WHERE estado = :estadoActual 
            AND fecha_hora + (duracion_min || ' minutes')::INTERVAL < :ahora
        """,
        nativeQuery = true
    )
    fun marcarReservasExpiradas(
        @Param("nuevoEstado") nuevoEstado: String,
        @Param("estadoActual") estadoActual: String,
        @Param("ahora") ahora: LocalDateTime
    ): Int
}