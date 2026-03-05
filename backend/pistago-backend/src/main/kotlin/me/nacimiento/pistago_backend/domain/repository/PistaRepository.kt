package me.nacimiento.pistago_backend.domain.repository

import me.nacimiento.pistago_backend.domain.entity.Pista
import me.nacimiento.pistago_backend.domain.enums.TipoPista
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PistaRepository : JpaRepository<Pista, Long> {
    fun findByActiva(activa: Boolean): List<Pista>
    fun findByTipo(tipo: TipoPista): List<Pista>
}