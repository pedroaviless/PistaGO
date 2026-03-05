package me.nacimiento.pistago_backend.domain.repository

import me.nacimiento.pistago_backend.domain.entity.Usuario
import me.nacimiento.pistago_backend.domain.enums.RolUsuario
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UsuarioRepository : JpaRepository<Usuario, Long> {
    fun findByEmail(email: String): Usuario?
    fun existsByEmail(email: String): Boolean
    fun findByRol(rol: RolUsuario): List<Usuario>
    fun findByActivo(activo: Boolean): List<Usuario>
}