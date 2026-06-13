package me.nacimiento.pistago_backend.domain.repository

import me.nacimiento.pistago_backend.domain.entity.BloqueoHorario
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime


//Este interface está prepara para trabajo futuro

@Repository
interface BloqueoHorarioRepository : JpaRepository<BloqueoHorario, Long> {
    fun findByPistaId(pistaId: Long): List<BloqueoHorario>
    fun findByPistaIdAndFechaHora(pistaId: Long, fechaHora: LocalDateTime): BloqueoHorario?
}