package me.nacimiento.pistago_backend.domain.repository

import me.nacimiento.pistago_backend.domain.entity.ListaEspera
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import org.springframework.data.jpa.repository.Modifying

import org.springframework.data.repository.query.Param


@Repository
interface ListaEsperaRepository : JpaRepository<ListaEspera, Long> {



    // ordenado por createdAt
    fun findByPistaIdAndFechaHoraOrderByCreatedAtAsc(pistaId: Long, fechaHora: LocalDateTime): List<ListaEspera>

    fun findByUsuarioIdAndPistaIdAndFechaHora(usuarioId: Long, pistaId: Long, fechaHora: LocalDateTime): ListaEspera?
    fun findByUsuarioId(usuarioId: Long): List<ListaEspera>

    @Query("""
    SELECT le FROM ListaEspera le 
    WHERE le.notificado = true 
      AND le.expiraEn IS NOT NULL 
      AND le.expiraEn < :ahora
""")
    fun findExpiradas(ahora: LocalDateTime): List<ListaEspera>

    @Modifying
    @Query("DELETE FROM ListaEspera l WHERE l.fechaHora < :ahora")
    fun deleteByFechaHoraBefore(@Param("ahora") ahora: LocalDateTime): Int

}