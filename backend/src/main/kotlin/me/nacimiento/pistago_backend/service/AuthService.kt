package me.nacimiento.pistago_backend.service

import me.nacimiento.pistago_backend.config.JwtService
import me.nacimiento.pistago_backend.domain.entity.Usuario
import me.nacimiento.pistago_backend.domain.enums.RolUsuario
import me.nacimiento.pistago_backend.domain.repository.UsuarioRepository
import me.nacimiento.pistago_backend.dto.AuthResponse
import me.nacimiento.pistago_backend.dto.LoginRequest
import me.nacimiento.pistago_backend.dto.PasswordChangeRequest
import me.nacimiento.pistago_backend.dto.PerfilRequest
import me.nacimiento.pistago_backend.dto.PerfilResponse
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

    fun updateFcmToken(email: String, fcmToken: String) {
        val usuario = usuarioRepository.findByEmail(email)
            ?: throw IllegalArgumentException("Usuario no encontrado")
        usuarioRepository.save(usuario.copy(fcmToken = fcmToken))
    }

    fun getPerfil(email: String): PerfilResponse {
        val usuario = usuarioRepository.findByEmail(email)
            ?: throw IllegalArgumentException("Usuario no encontrado")
        return usuario.toPerfilResponse()
    }

    fun actualizarPerfil(email: String, request: PerfilRequest): PerfilResponse {
        val usuario = usuarioRepository.findByEmail(email)
            ?: throw IllegalArgumentException("Usuario no encontrado")

        val actualizado = usuario.copy(
            nombre = request.nombre,
            telefono = request.telefono,
            updatedAt = LocalDateTime.now()
        )

        val saved = usuarioRepository.save(actualizado)
        return saved.toPerfilResponse()
    }

    fun cambiarPassword(email: String, request: PasswordChangeRequest) {
        val usuario = usuarioRepository.findByEmail(email)
            ?: throw IllegalArgumentException("Usuario no encontrado")

        if (!passwordEncoder.matches(request.passwordActual, usuario.passwordHash)) {
            throw IllegalArgumentException("Contraseña actual incorrecta")
        }

        require(request.passwordNueva.length >= 8) {
            "La nueva contraseña debe tener al menos 8 caracteres"
        }

        usuarioRepository.save(
            usuario.copy(
                passwordHash = passwordEncoder.encode(request.passwordNueva),
                updatedAt = LocalDateTime.now()
            )
        )
    }

    fun actualizarFotoPerfil(email: String, nuevaUrl: String): PerfilResponse {
        val usuario = usuarioRepository.findByEmail(email)
            ?: throw IllegalArgumentException("Usuario no encontrado")

        val saved = usuarioRepository.save(
            usuario.copy(
                fotoUrl = nuevaUrl,
                updatedAt = LocalDateTime.now()
            )
        )
        return saved.toPerfilResponse()
    }

    fun obtenerIdPorEmail(email: String): Long =
        usuarioRepository.findByEmail(email)?.id
            ?: throw IllegalArgumentException("Usuario no encontrado")

    private fun Usuario.toPerfilResponse() = PerfilResponse(
        nombre = nombre,
        email = email,
        telefono = telefono,
        fotoUrl = fotoUrl,
        rol = rol.name
    )
}