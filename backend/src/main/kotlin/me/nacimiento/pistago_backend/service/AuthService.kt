package me.nacimiento.pistago_backend.service

import me.nacimiento.pistago_backend.config.JwtService
import me.nacimiento.pistago_backend.domain.entity.Usuario
import me.nacimiento.pistago_backend.domain.enums.RolUsuario
import me.nacimiento.pistago_backend.domain.repository.UsuarioRepository
import me.nacimiento.pistago_backend.dto.AuthResponse
import me.nacimiento.pistago_backend.dto.LoginRequest
import me.nacimiento.pistago_backend.dto.RegisterRequest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AuthService(
    private val usuarioRepository: UsuarioRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {

    fun register(request: RegisterRequest): AuthResponse {
        if (usuarioRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("El email ya está registrado")
        }

        val usuario = Usuario(
            nombre = request.nombre,
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password),
            rol = RolUsuario.USUARIO,
            activo = true,
            reservasSemana = 0,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val saved = usuarioRepository.save(usuario)
        val token = jwtService.generateToken(saved.email, saved.rol.name)

        return AuthResponse(
            token = token,
            email = saved.email,
            nombre = saved.nombre,
            rol = saved.rol.name
        )
    }

    fun login(request: LoginRequest): AuthResponse {
        val usuario = usuarioRepository.findByEmail(request.email)
            ?: throw IllegalArgumentException("Credenciales incorrectas")

        if (!usuario.activo) {
            throw IllegalArgumentException("Usuario desactivado")
        }

        if (!passwordEncoder.matches(request.password, usuario.passwordHash)) {
            throw IllegalArgumentException("Credenciales incorrectas")
        }

        val token = jwtService.generateToken(usuario.email, usuario.rol.name)

        return AuthResponse(
            token = token,
            email = usuario.email,
            nombre = usuario.nombre,
            rol = usuario.rol.name
        )
    }
}