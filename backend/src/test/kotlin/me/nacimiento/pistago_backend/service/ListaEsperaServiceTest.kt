package me.nacimiento.pistago_backend.service

import me.nacimiento.pistago_backend.domain.entity.ListaEspera
import me.nacimiento.pistago_backend.domain.entity.Pista
import me.nacimiento.pistago_backend.domain.entity.Usuario
import me.nacimiento.pistago_backend.domain.repository.ListaEsperaRepository
import me.nacimiento.pistago_backend.domain.repository.PistaRepository
import me.nacimiento.pistago_backend.domain.repository.UsuarioRepository
import me.nacimiento.pistago_backend.dto.ListaEsperaRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.kotlin.mock
import java.time.LocalDateTime
import java.util.Optional

@DisplayName("Tests unitarios de ListaEsperaService")
class ListaEsperaServiceTest {

    private lateinit var listaEsperaRepository: ListaEsperaRepository
    private lateinit var pistaRepository: PistaRepository
    private lateinit var usuarioRepository: UsuarioRepository
    private lateinit var listaEsperaService: ListaEsperaService

    private val usuario = Usuario(id = 1, nombre = "Lucía", email = "lucia@pistago.com")
    private val pista = Pista(id = 1, nombre = "Pista 1", activa = true)
    private val fechaHora = LocalDateTime.of(2026, 6, 25, 18, 0)

    @BeforeEach
    fun setUp() {
        listaEsperaRepository = mock()
        pistaRepository = mock()
        usuarioRepository = mock()
        listaEsperaService = ListaEsperaService(listaEsperaRepository, pistaRepository, usuarioRepository)
    }

    // ---------------------------------------------------------------
    //  apuntarse()
    // ---------------------------------------------------------------

    @Test
    @DisplayName("apuntarse: añade al usuario a la lista de espera correctamente")
    fun apuntarse_ok() {
        val request = ListaEsperaRequest(pistaId = 1, fechaHora = fechaHora)
        whenever(usuarioRepository.findByEmail("lucia@pistago.com")).thenReturn(usuario)
        whenever(pistaRepository.findById(1)).thenReturn(Optional.of(pista))
        whenever(listaEsperaRepository.findByUsuarioIdAndPistaIdAndFechaHora(1, 1, fechaHora))
            .thenReturn(null)
        whenever(listaEsperaRepository.findByPistaIdAndFechaHoraOrderByCreatedAtAsc(1, fechaHora))
            .thenReturn(emptyList())
        whenever(listaEsperaRepository.save(any<ListaEspera>())).thenAnswer { invocation ->
            (invocation.arguments[0] as ListaEspera).copy(id = 7)
        }

        val resultado = listaEsperaService.apuntarse("lucia@pistago.com", request)

        assertNotNull(resultado)
        assertEquals(1, resultado.pistaId)
        verify(listaEsperaRepository).save(any<ListaEspera>())
    }

    @Test
    @DisplayName("apuntarse: lanza excepción si el usuario ya está en la lista de esa franja")
    fun apuntarse_yaEnLista() {
        val request = ListaEsperaRequest(pistaId = 1, fechaHora = fechaHora)
        val entradaExistente = ListaEspera(id = 3, usuario = usuario, pista = pista, fechaHora = fechaHora)
        whenever(usuarioRepository.findByEmail("lucia@pistago.com")).thenReturn(usuario)
        whenever(pistaRepository.findById(1)).thenReturn(Optional.of(pista))
        whenever(listaEsperaRepository.findByUsuarioIdAndPistaIdAndFechaHora(1, 1, fechaHora))
            .thenReturn(entradaExistente)

        val ex = assertThrows(IllegalArgumentException::class.java) {
            listaEsperaService.apuntarse("lucia@pistago.com", request)
        }
        assertEquals("Ya estás en la lista de espera para este horario", ex.message)
        verify(listaEsperaRepository, never()).save(any<ListaEspera>())
    }

    @Test
    @DisplayName("apuntarse: lanza excepción si la pista no existe")
    fun apuntarse_pistaNoExiste() {
        val request = ListaEsperaRequest(pistaId = 99, fechaHora = fechaHora)
        whenever(usuarioRepository.findByEmail("lucia@pistago.com")).thenReturn(usuario)
        whenever(pistaRepository.findById(99)).thenReturn(Optional.empty())

        val ex = assertThrows(IllegalArgumentException::class.java) {
            listaEsperaService.apuntarse("lucia@pistago.com", request)
        }
        assertEquals("Pista no encontrada", ex.message)
        verify(listaEsperaRepository, never()).save(any<ListaEspera>())
    }

    @Test
    @DisplayName("apuntarse: lanza excepción si el usuario no existe")
    fun apuntarse_usuarioNoExiste() {
        val request = ListaEsperaRequest(pistaId = 1, fechaHora = fechaHora)
        whenever(usuarioRepository.findByEmail("desconocido@pistago.com")).thenReturn(null)

        val ex = assertThrows(IllegalArgumentException::class.java) {
            listaEsperaService.apuntarse("desconocido@pistago.com", request)
        }
        assertEquals("Usuario no encontrado", ex.message)
        verify(listaEsperaRepository, never()).save(any<ListaEspera>())
    }

    // ---------------------------------------------------------------
    //  salir()
    // ---------------------------------------------------------------

    @Test
    @DisplayName("salir: elimina la entrada propia de la lista de espera")
    fun salir_ok() {
        val entrada = ListaEspera(id = 5, usuario = usuario, pista = pista, fechaHora = fechaHora)
        whenever(usuarioRepository.findByEmail("lucia@pistago.com")).thenReturn(usuario)
        whenever(listaEsperaRepository.findById(5)).thenReturn(Optional.of(entrada))

        listaEsperaService.salir("lucia@pistago.com", 5)

        verify(listaEsperaRepository).delete(entrada)
    }

    @Test
    @DisplayName("salir: lanza excepción si la entrada no es del usuario")
    fun salir_noAutorizado() {
        val otroUsuario = Usuario(id = 2, nombre = "Mateo", email = "mateo@pistago.com")
        val entrada = ListaEspera(id = 5, usuario = otroUsuario, pista = pista, fechaHora = fechaHora)
        whenever(usuarioRepository.findByEmail("lucia@pistago.com")).thenReturn(usuario)
        whenever(listaEsperaRepository.findById(5)).thenReturn(Optional.of(entrada))

        val ex = assertThrows(IllegalArgumentException::class.java) {
            listaEsperaService.salir("lucia@pistago.com", 5)
        }
        assertEquals("No autorizado", ex.message)
        verify(listaEsperaRepository, never()).delete(any<ListaEspera>())
    }

    @Test
    @DisplayName("salir: lanza excepción si la entrada no existe")
    fun salir_entradaNoExiste() {
        whenever(usuarioRepository.findByEmail("lucia@pistago.com")).thenReturn(usuario)
        whenever(listaEsperaRepository.findById(99)).thenReturn(Optional.empty())

        assertThrows(NoSuchElementException::class.java) {
            listaEsperaService.salir("lucia@pistago.com", 99)
        }
        verify(listaEsperaRepository, never()).delete(any<ListaEspera>())
    }
}