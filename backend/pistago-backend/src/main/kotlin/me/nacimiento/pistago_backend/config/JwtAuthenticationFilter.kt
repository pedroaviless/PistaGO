package me.nacimiento.pistago_backend.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import me.nacimiento.pistago_backend.domain.repository.UsuarioRepository
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val usuarioRepository: UsuarioRepository
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val token = authHeader.substring(7)

        if (!jwtService.isTokenValid(token)) {
            filterChain.doFilter(request, response)
            return
        }

        val email = jwtService.extractEmail(token)
        val rol = jwtService.extractRol(token)

        if (SecurityContextHolder.getContext().authentication == null) {
            val usuario = usuarioRepository.findByEmail(email)
            if (usuario != null && usuario.activo) {
                val authorities = listOf(SimpleGrantedAuthority("ROLE_$rol"))
                val authToken = UsernamePasswordAuthenticationToken(
                    email, null, authorities
                )
                authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authToken
            }
        }

        filterChain.doFilter(request, response)
    }
}