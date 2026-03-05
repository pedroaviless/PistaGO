package me.nacimiento.pistago_backend.domain.repository

import me.nacimiento.pistago_backend.domain.entity.Notificacion
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NotificacionRepository : JpaRepository<Notificacion, Long> {
    fun findByUsuarioIdOrderByCreatedAtDesc(usuarioId: Long): List<Notificacion>
    fun findByUsuarioIdAndLeida(usuarioId: Long, leida: Boolean): List<Notificacion>
    fun countByUsuarioIdAndLeida(usuarioId: Long, leida: Boolean): Long
}