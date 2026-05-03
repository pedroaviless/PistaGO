package me.nacimiento.pistago_backend.controller

import me.nacimiento.pistago_backend.config.JwtService
import me.nacimiento.pistago_backend.dto.AuthResponse
import me.nacimiento.pistago_backend.dto.LoginRequest
import me.nacimiento.pistago_backend.dto.PerfilRequest
import me.nacimiento.pistago_backend.dto.PerfilResponse
import me.nacimiento.pistago_backend.dto.RegisterRequest
import me.nacimiento.pistago_backend.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val jwtService: JwtService
) {
    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> =
        ResponseEntity.ok(authService.register(request))

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> =
        ResponseEntity.ok(authService.login(request))

    @PostMapping("/fcm-token")
    fun updateFcmToken(
        @RequestHeader("Authorization") authHeader: String,
        @RequestBody body: Map<String, String>
    ): ResponseEntity<Void> {
        val token = authHeader.removePrefix("Bearer ")
        val email = jwtService.extractEmail(token)
        authService.updateFcmToken(email, body["fcmToken"] ?: return ResponseEntity.badRequest().build())
        return ResponseEntity.ok().build()
    }
}