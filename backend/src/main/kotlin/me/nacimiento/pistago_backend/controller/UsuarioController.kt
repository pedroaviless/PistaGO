package me.nacimiento.pistago_backend.controller

import jakarta.validation.Valid
import me.nacimiento.pistago_backend.config.JwtService
import me.nacimiento.pistago_backend.dto.PasswordChangeRequest
import me.nacimiento.pistago_backend.dto.PerfilRequest
import me.nacimiento.pistago_backend.dto.PerfilResponse
import me.nacimiento.pistago_backend.service.AuthService
import me.nacimiento.pistago_backend.service.AvatarStorageService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/usuarios")
class UsuarioController(
    private val authService: AuthService,
    private val avatarStorageService: AvatarStorageService,
    private val jwtService: JwtService
) {
    @GetMapping("/perfil")
    fun getPerfil(
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<PerfilResponse> {
        val email = jwtService.extractEmail(authHeader.removePrefix("Bearer "))
        return ResponseEntity.ok(authService.getPerfil(email))
    }

    @PutMapping("/perfil")
    fun actualizarPerfil(
        @RequestHeader("Authorization") authHeader: String,
        @Valid @RequestBody request: PerfilRequest
    ): ResponseEntity<PerfilResponse> {
        val email = jwtService.extractEmail(authHeader.removePrefix("Bearer "))
        return ResponseEntity.ok(authService.actualizarPerfil(email, request))
    }

    @PutMapping("/perfil/password")
    fun cambiarPassword(
        @RequestHeader("Authorization") authHeader: String,
        @Valid @RequestBody request: PasswordChangeRequest
    ): ResponseEntity<Void> {
        val email = jwtService.extractEmail(authHeader.removePrefix("Bearer "))
        authService.cambiarPassword(email, request)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/perfil/foto", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun subirFoto(
        @RequestHeader("Authorization") authHeader: String,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<PerfilResponse> {
        val email = jwtService.extractEmail(authHeader.removePrefix("Bearer "))
        val perfilActual = authService.getPerfil(email)

        // Borramos la foto antigua (si existía) antes de subir la nueva
        avatarStorageService.borrar(perfilActual.fotoUrl)

        // Necesitamos el id del usuario para nombrar el archivo
        val usuarioId = authService.obtenerIdPorEmail(email)
        val nuevaUrl = avatarStorageService.guardar(file, usuarioId)

        return ResponseEntity.ok(authService.actualizarFotoPerfil(email, nuevaUrl))
    }
}