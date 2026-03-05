package me.nacimiento.pistago_backend.config

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date

@Service
class JwtService {

    @Value("\${pistago.jwt.secret}")
    private lateinit var secret: String

    @Value("\${pistago.jwt.expiration}")
    private var expiration: Long = 0

    private val signingKey get() = Keys.hmacShaKeyFor(secret.toByteArray())

    fun generateToken(email: String, rol: String): String {
        return Jwts.builder()
            .subject(email)
            .claim("rol", rol)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expiration))
            .signWith(signingKey)
            .compact()
    }

    fun extractEmail(token: String): String {
        return extractClaims(token).subject
    }

    fun extractRol(token: String): String {
        return extractClaims(token)["rol"] as String
    }

    fun isTokenValid(token: String): Boolean {
        return try {
            extractClaims(token).expiration.after(Date())
        } catch (e: Exception) {
            false
        }
    }

    private fun extractClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}