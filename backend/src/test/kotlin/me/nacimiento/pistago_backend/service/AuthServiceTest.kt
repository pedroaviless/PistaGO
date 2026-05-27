package me.nacimiento.pistago_backend.service

import me.nacimiento.pistago_backend.config.JwtService
import me.nacimiento.pistago_backend.domain.entity.Usuario
import me.nacimiento.pistago_backend.domain.enums.RolUsuario
import me.nacimiento.pistago_backend.domain.repository.UsuarioRepository
import me.nacimiento.pistago_backend.dto.LoginRequest
import me.nacimiento.pistago_backend.dto.PasswordChangeRequest
import me.nacimiento.pistago_backend.dto.RegisterRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.kotlin.mock
import org.springframework.security.crypto.password.PasswordEncoder

@DisplayName("Tests unitarios de AuthService")
class AuthServiceTest {

    private lateinit var usuarioRepository: UsuarioRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var jwtService: JwtService
    private lateinit var authService: AuthService

    private val usuario = Usuario(
        id = 1,
        nombre = "Lucía",
        email = "lucia@pistago.com",
        passwordHash = "hash-guardado",
        rol = RolUsuario.USUARIO,
        activo = true
    )

    @BeforeEach
    fun setUp() {
        usuarioRepository = mock()
        passwordEncoder = mock()
        jwtService = mock()
        authService = AuthService(usuarioRepository, passwordEncoder, jwtService)
    }

    // ---------------------------------------------------------------
    //  register()
    // ---------------------------------------------------------------

    @Test
    @DisplayName("register: registra un usuario nuevo y devuelve token")
    fun register_ok() {
        val request = RegisterRequest(nombre = "Nuevo", email = "nuevo@pistago.com", password = "password123")
        whenever(usuarioRepository.existsByEmail("nuevo@pistago.com")).thenReturn(false)
        whenever(passwordEncoder.encode("password123")).thenReturn("hash-cifrado")
        whenever(usuarioRepository.save(any<Usuario>())).thenAnswer { invocation ->
            (invocation.arguments[0] as Usuario).copy(id = 5)
        }
        whenever(jwtService.generateToken("nuevo@pistago.com", "USUARIO")).thenReturn("token-jwt")

        val resultado = authService.register(request)

        assertEquals("token-jwt", resultado.token)
        assertEquals("nuevo@pistago.com", resultado.email)
        assertEquals("USUARIO", resultado.rol)
        // La contraseña se cifró (nunca se guarda en claro)
        verify(passwordEncoder).encode("password123")
        verify(usuarioRepository).save(any<Usuario>())
    }

    @Test
    @DisplayName("register: lanza excepción si el email ya está registrado")
    fun register_emailDuplicado() {
        val request = RegisterRequest(nombre = "Lucía", email = "lucia@pistago.com", password = "password123")
        whenever(usuarioRepository.existsByEmail("lucia@pistago.com")).thenReturn(true)

        val ex = assertThrows(IllegalArgumentException::class.java) {
            authService.register(request)
        }
        assertEquals("El email ya está registrado", ex.message)
        verify(usuarioRepository, never()).save(any<Usuario>())
    }

    // ---------------------------------------------------------------
    //  login()
    // ---------------------------------------------------------------

    @Test
    @DisplayName("login: autentica correctamente con credenciales válidas")
    fun login_ok() {
        val request = LoginRequest(email = "lucia@pistago.com", password = "password123")
        whenever(usuarioRepository.findByEmail("lucia@pistago.com")).thenReturn(usuario)
        whenever(passwordEncoder.matches("password123", "hash-guardado")).thenReturn(true)
        whenever(jwtService.generateToken("lucia@pistago.com", "USUARIO")).thenReturn("token-jwt")

        val resultado = authService.login(request)

        assertEquals("token-jwt", resultado.token)
        assertEquals("lucia@pistago.com", resultado.email)
    }

    @Test
    @DisplayName("login: lanza excepción si el usuario no existe")
    fun login_usuarioNoExiste() {
        val request = LoginRequest(email = "desconocido@pistago.com", password = "password123")
        whenever(usuarioRepository.findByEmail("desconocido@pistago.com")).thenReturn(null)

        val ex = assertThrows(IllegalArgumentException::class.java) {
            authService.login(request)
        }
        assertEquals("Credenciales incorrectas", ex.message)
    }

    @Test
    @DisplayName("login: lanza excepción si la contraseña es incorrecta")
    fun login_passwordIncorrecta() {
        val request = LoginRequest(email = "lucia@pistago.com", password = "mala")
        whenever(usuarioRepository.findByEmail("lucia@pistago.com")).thenReturn(usuario)
        whenever(passwordEncoder.matches("mala", "hash-guardado")).thenReturn(false)

        val ex = assertThrows(IllegalArgumentException::class.java) {
            authService.login(request)
        }
        assertEquals("Credenciales incorrectas", ex.message)
    }

    @Test
    @DisplayName("login: lanza excepción si el usuario está desactivado")
    fun login_usuarioDesactivado() {
        val inactivo = usuario.copy(activo = false)
        val request = LoginRequest(email = "lucia@pistago.com", password = "password123")
        whenever(usuarioRepository.findByEmail("lucia@pistago.com")).thenReturn(inactivo)

        val ex = assertThrows(IllegalArgumentException::class.java) {
            authService.login(request)
        }
        assertEquals("Usuario desactivado", ex.message)
    }

    // ---------------------------------------------------------------
    //  cambiarPassword()
    // ---------------------------------------------------------------

    @Test
    @DisplayName("cambiarPassword: cambia la contraseña con datos válidos")
    fun cambiarPassword_ok() {
        val request = PasswordChangeRequest(passwordActual = "password123", passwordNueva = "nuevaPassword123")
        whenever(usuarioRepository.findByEmail("lucia@pistago.com")).thenReturn(usuario)
        whenever(passwordEncoder.matches("password123", "hash-guardado")).thenReturn(true)
        whenever(passwordEncoder.encode("nuevaPassword123")).thenReturn("nuevo-hash")
        whenever(usuarioRepository.save(any<Usuario>())).thenAnswer { it.arguments[0] as Usuario }

        authService.cambiarPassword("lucia@pistago.com", request)

        verify(passwordEncoder).encode("nuevaPassword123")
        verify(usuarioRepository).save(any<Usuario>())
    }

    @Test
    @DisplayName("cambiarPassword: lanza excepción si la contraseña actual es incorrecta")
    fun cambiarPassword_actualIncorrecta() {
        val request = PasswordChangeRequest(passwordActual = "mala", passwordNueva = "nuevaPassword123")
        whenever(usuarioRepository.findByEmail("lucia@pistago.com")).thenReturn(usuario)
        whenever(passwordEncoder.matches("mala", "hash-guardado")).thenReturn(false)

        val ex = assertThrows(IllegalArgumentException::class.java) {
            authService.cambiarPassword("lucia@pistago.com", request)
        }
        assertEquals("Contraseña actual incorrecta", ex.message)
        verify(usuarioRepository, never()).save(any<Usuario>())
    }

    @Test
    @DisplayName("cambiarPassword: lanza excepción si la nueva contraseña es demasiado corta")
    fun cambiarPassword_nuevaCorta() {
        val request = PasswordChangeRequest(passwordActual = "password123", passwordNueva = "corta")
        whenever(usuarioRepository.findByEmail("lucia@pistago.com")).thenReturn(usuario)
        whenever(passwordEncoder.matches("password123", "hash-guardado")).thenReturn(true)

        assertThrows(IllegalArgumentException::class.java) {
            authService.cambiarPassword("lucia@pistago.com", request)
        }
        verify(usuarioRepository, never()).save(any<Usuario>())
    }
}