package me.nacimiento.pistago_backend.service

import me.nacimiento.pistago_backend.domain.entity.Pista
import me.nacimiento.pistago_backend.domain.entity.Reserva
import me.nacimiento.pistago_backend.domain.entity.Usuario
import me.nacimiento.pistago_backend.domain.entity.ListaEspera
import me.nacimiento.pistago_backend.domain.enums.EstadoReserva
import me.nacimiento.pistago_backend.domain.enums.RolUsuario
import me.nacimiento.pistago_backend.domain.repository.ListaEsperaRepository
import me.nacimiento.pistago_backend.domain.repository.NotificacionRepository
import me.nacimiento.pistago_backend.domain.repository.PistaRepository
import me.nacimiento.pistago_backend.domain.repository.ReservaRepository
import me.nacimiento.pistago_backend.domain.repository.UsuarioRepository
import me.nacimiento.pistago_backend.dto.ReservaRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.kotlin.mock
import java.time.LocalDateTime
import java.util.Optional

@DisplayName("Tests unitarios de ReservaService")
class ReservaServiceTest {

    private lateinit var reservaRepository: ReservaRepository
    private lateinit var pistaRepository: PistaRepository
    private lateinit var usuarioRepository: UsuarioRepository
    private lateinit var listaEsperaRepository: ListaEsperaRepository
    private lateinit var notificacionRepository: NotificacionRepository
    private lateinit var fcmService: FcmService
    private lateinit var reservaService: ReservaService

    // Datos de prueba reutilizables
    private val usuario = Usuario(id = 1, nombre = "Lucía", email = "lucia@pistago.com")
    private val pista = Pista(id = 1, nombre = "Pista 1", activa = true)
    private val fechaHora = LocalDateTime.of(2026, 6, 25, 18, 0)

    @BeforeEach
    fun setUp() {
        reservaRepository = mock()
        pistaRepository = mock()
        usuarioRepository = mock()
        listaEsperaRepository = mock()
        notificacionRepository = mock()
        fcmService = mock()
        reservaService = ReservaService(
            reservaRepository,
            pistaRepository,
            usuarioRepository,
            listaEsperaRepository,
            notificacionRepository,
            fcmService
        )
    }

    // ---------------------------------------------------------------
    //  crear()
    // ---------------------------------------------------------------

    @Test
    @DisplayName("crear: reserva correctamente cuando todos los datos son válidos")
    fun crear_ok() {
        val request = ReservaRequest(pistaId = 1, fechaHora = fechaHora, duracionMin = 60)
        whenever(usuarioRepository.findByEmail("lucia@pistago.com")).thenReturn(usuario)
        whenever(pistaRepository.findById(1)).thenReturn(Optional.of(pista))
        whenever(reservaRepository.findReservaActiva(1, fechaHora)).thenReturn(null)
        whenever(reservaRepository.save(any<Reserva>())).thenAnswer { invocation ->
            (invocation.arguments[0] as Reserva).copy(id = 100)
        }

        val resultado = reservaService.crear("lucia@pistago.com", request)

        assertNotNull(resultado)
        assertEquals(1, resultado.pistaId)
        assertEquals("Pista 1", resultado.nombrePista)
        assertEquals(EstadoReserva.CONFIRMADA, resultado.estado)
        verify(reservaRepository).save(any<Reserva>())
    }

    @Test
    @DisplayName("crear: lanza excepción si el usuario no existe")
    fun crear_usuarioNoExiste() {
        val request = ReservaRequest(pistaId = 1, fechaHora = fechaHora, duracionMin = 60)
        whenever(usuarioRepository.findByEmail("desconocido@pistago.com")).thenReturn(null)

        val ex = assertThrows(IllegalArgumentException::class.java) {
            reservaService.crear("desconocido@pistago.com", request)
        }
        assertEquals("Usuario no encontrado", ex.message)
        verify(reservaRepository, never()).save(any<Reserva>())
    }

    @Test
    @DisplayName("crear: lanza excepción si la pista no existe")
    fun crear_pistaNoExiste() {
        val request = ReservaRequest(pistaId = 99, fechaHora = fechaHora, duracionMin = 60)
        whenever(usuarioRepository.findByEmail("lucia@pistago.com")).thenReturn(usuario)
        whenever(pistaRepository.findById(99)).thenReturn(Optional.empty())

        val ex = assertThrows(IllegalArgumentException::class.java) {
            reservaService.crear("lucia@pistago.com", request)
        }
        assertEquals("Pista no encontrada", ex.message)
        verify(reservaRepository, never()).save(any<Reserva>())
    }

    @Test
    @DisplayName("crear: lanza excepción si la pista no está activa")
    fun crear_pistaInactiva() {
        val pistaInactiva = Pista(id = 2, nombre = "Pista 2", activa = false)
        val request = ReservaRequest(pistaId = 2, fechaHora = fechaHora, duracionMin = 60)
        whenever(usuarioRepository.findByEmail("lucia@pistago.com")).thenReturn(usuario)
        whenever(pistaRepository.findById(2)).thenReturn(Optional.of(pistaInactiva))

        val ex = assertThrows(IllegalArgumentException::class.java) {
            reservaService.crear("lucia@pistago.com", request)
        }
        assertEquals("La pista no está activa", ex.message)
        verify(reservaRepository, never()).save(any<Reserva>())
    }

    @Test
    @DisplayName("crear: lanza excepción si ya existe una reserva en esa franja (solapamiento)")
    fun crear_solapamiento() {
        val request = ReservaRequest(pistaId = 1, fechaHora = fechaHora, duracionMin = 60)
        val reservaExistente = Reserva(id = 50, usuario = usuario, pista = pista, fechaHora = fechaHora)
        whenever(usuarioRepository.findByEmail("lucia@pistago.com")).thenReturn(usuario)
        whenever(pistaRepository.findById(1)).thenReturn(Optional.of(pista))
        whenever(reservaRepository.findReservaActiva(1, fechaHora)).thenReturn(reservaExistente)

        val ex = assertThrows(IllegalArgumentException::class.java) {
            reservaService.crear("lucia@pistago.com", request)
        }
        assertEquals("La pista ya está reservada en ese horario", ex.message)
        verify(reservaRepository, never()).save(any<Reserva>())
    }
    @Test
    @DisplayName("crear: lanza excepción si el usuario ya tiene una reserva activa")
    fun crear_yaTieneReservaActiva() {
        val ahoraMas2h = LocalDateTime.now().plusHours(2)
        val request = ReservaRequest(pistaId = 1, fechaHora = fechaHora, duracionMin = 90)
        val reservaActiva = Reserva(
            id = 50,
            usuario = usuario,
            pista = pista,
            fechaHora = ahoraMas2h,
            duracionMin = 90,
            estado = EstadoReserva.CONFIRMADA
        )
        whenever(usuarioRepository.findByEmail("lucia@pistago.com")).thenReturn(usuario)
        whenever(pistaRepository.findById(1)).thenReturn(Optional.of(pista))
        whenever(reservaRepository.findReservasConfirmadasDesde(any(), any()))
            .thenReturn(listOf(reservaActiva))

        val ex = assertThrows(IllegalArgumentException::class.java) {
            reservaService.crear("lucia@pistago.com", request)
        }
        assertEquals("Ya tienes una reserva activa. Espera a que termine para reservar otra.", ex.message)
        verify(reservaRepository, never()).save(any<Reserva>())
    }

    @Test
    @DisplayName("crear: permite reservar si la reserva anterior ya ha terminado")
    fun crear_reservaAnteriorExpirada() {
        val haceTresHoras = LocalDateTime.now().minusHours(3)
        val request = ReservaRequest(pistaId = 1, fechaHora = fechaHora, duracionMin = 90)
        val reservaExpirada = Reserva(
            id = 50,
            usuario = usuario,
            pista = pista,
            fechaHora = haceTresHoras,    // empezó hace 3h
            duracionMin = 90,             // duró 1h30, así que terminó hace 1h30
            estado = EstadoReserva.CONFIRMADA
        )
        whenever(usuarioRepository.findByEmail("lucia@pistago.com")).thenReturn(usuario)
        whenever(pistaRepository.findById(1)).thenReturn(Optional.of(pista))
        whenever(reservaRepository.findReservasConfirmadasDesde(any(), any()))
            .thenReturn(listOf(reservaExpirada))
        whenever(reservaRepository.findReservaActiva(1, fechaHora)).thenReturn(null)
        whenever(reservaRepository.save(any<Reserva>())).thenAnswer { invocation ->
            (invocation.arguments[0] as Reserva).copy(id = 100)
        }

        val resultado = reservaService.crear("lucia@pistago.com", request)

        assertNotNull(resultado)
        assertEquals(EstadoReserva.CONFIRMADA, resultado.estado)
        verify(reservaRepository).save(any<Reserva>())
    }

    @Test
    @DisplayName("crear: el administrador queda exento de la regla de reserva única")
    fun crear_adminExentoDeRegla() {
        val admin = Usuario(
            id = 99,
            nombre = "Admin",
            email = "admin@pistago.com",
            rol = RolUsuario.ADMINISTRADOR
        )
        val ahoraMas2h = LocalDateTime.now().plusHours(2)
        val request = ReservaRequest(pistaId = 1, fechaHora = fechaHora, duracionMin = 90)
        val reservaActivaDelAdmin = Reserva(
            id = 50,
            usuario = admin,
            pista = pista,
            fechaHora = ahoraMas2h,
            duracionMin = 90,
            estado = EstadoReserva.CONFIRMADA
        )
        whenever(usuarioRepository.findByEmail("admin@pistago.com")).thenReturn(admin)
        whenever(pistaRepository.findById(1)).thenReturn(Optional.of(pista))
        whenever(reservaRepository.findReservaActiva(1, fechaHora)).thenReturn(null)
        whenever(reservaRepository.save(any<Reserva>())).thenAnswer { invocation ->
            (invocation.arguments[0] as Reserva).copy(id = 100)
        }
        // Nota: NO mockeamos findReservasConfirmadasDesde porque el admin no debe llegar a esa comprobación

        val resultado = reservaService.crear("admin@pistago.com", request)

        assertNotNull(resultado)
        verify(reservaRepository).save(any<Reserva>())
        // Verificamos que NUNCA se llama a findReservasConfirmadasDesde para un admin (queda exento)
        verify(reservaRepository, never()).findReservasConfirmadasDesde(any(), any())
    }

    // ---------------------------------------------------------------
    //  cancelar()
    // ---------------------------------------------------------------

    @Test
    @DisplayName("cancelar: cancela correctamente una reserva propia confirmada")
    fun cancelar_ok() {
        val reserva = Reserva(id = 10, usuario = usuario, pista = pista, fechaHora = fechaHora,
            estado = EstadoReserva.CONFIRMADA)
        whenever(usuarioRepository.findByEmail("lucia@pistago.com")).thenReturn(usuario)
        whenever(reservaRepository.findById(10)).thenReturn(Optional.of(reserva))
        whenever(reservaRepository.save(any<Reserva>())).thenAnswer { it.arguments[0] as Reserva }
        whenever(listaEsperaRepository.findByPistaIdAndFechaHoraOrderByCreatedAtAsc(1, fechaHora))
            .thenReturn(emptyList())

        val resultado = reservaService.cancelar("lucia@pistago.com", 10)

        assertEquals(EstadoReserva.CANCELADA, resultado.estado)
        verify(reservaRepository).save(any<Reserva>())
    }

    @Test
    @DisplayName("cancelar: lanza excepción si la reserva no es del usuario")
    fun cancelar_noAutorizado() {
        val otroUsuario = Usuario(id = 2, nombre = "Mateo", email = "mateo@pistago.com")
        val reserva = Reserva(id = 10, usuario = otroUsuario, pista = pista, fechaHora = fechaHora,
            estado = EstadoReserva.CONFIRMADA)
        whenever(usuarioRepository.findByEmail("lucia@pistago.com")).thenReturn(usuario)
        whenever(reservaRepository.findById(10)).thenReturn(Optional.of(reserva))

        val ex = assertThrows(IllegalArgumentException::class.java) {
            reservaService.cancelar("lucia@pistago.com", 10)
        }
        assertEquals("No autorizado", ex.message)
        verify(reservaRepository, never()).save(any<Reserva>())
    }

    @Test
    @DisplayName("cancelar: lanza excepción si la reserva no está confirmada")
    fun cancelar_noConfirmada() {
        val reserva = Reserva(id = 10, usuario = usuario, pista = pista, fechaHora = fechaHora,
            estado = EstadoReserva.CANCELADA)
        whenever(usuarioRepository.findByEmail("lucia@pistago.com")).thenReturn(usuario)
        whenever(reservaRepository.findById(10)).thenReturn(Optional.of(reserva))

        val ex = assertThrows(IllegalArgumentException::class.java) {
            reservaService.cancelar("lucia@pistago.com", 10)
        }
        assertEquals("La reserva no se puede cancelar", ex.message)
        verify(reservaRepository, never()).save(any<Reserva>())
    }

    @Test
    @DisplayName("cancelar: notifica al primero de la lista de espera al cancelar")
    fun cancelar_notificaPrimeroEnEspera() {
        val reserva = Reserva(id = 10, usuario = usuario, pista = pista, fechaHora = fechaHora,
            estado = EstadoReserva.CONFIRMADA)
        val enEspera = Usuario(id = 3, nombre = "Carlos", email = "carlos@pistago.com",
            fcmToken = "token-fcm-carlos")
        val entradaCola = ListaEspera(id = 1, usuario = enEspera, pista = pista,
            fechaHora = fechaHora, notificado = false)

        whenever(usuarioRepository.findByEmail("lucia@pistago.com")).thenReturn(usuario)
        whenever(reservaRepository.findById(10)).thenReturn(Optional.of(reserva))
        whenever(reservaRepository.save(any<Reserva>())).thenAnswer { it.arguments[0] as Reserva }
        whenever(listaEsperaRepository.findByPistaIdAndFechaHoraOrderByCreatedAtAsc(1, fechaHora))
            .thenReturn(listOf(entradaCola))
        whenever(fcmService.enviarNotificacion(any(), any(), any(), any())).thenReturn(true)

        reservaService.cancelar("lucia@pistago.com", 10)

        // Se envió la push al usuario en espera
        verify(fcmService).enviarNotificacion(any(), any(), any(), any())
        // Se marcó como notificado (save en lista de espera)
        verify(listaEsperaRepository).save(any<ListaEspera>())
        // Se registró la notificación en BD
        verify(notificacionRepository).save(any())
    }

    @Test
    @DisplayName("cancelar: no envía notificación si la lista de espera está vacía")
    fun cancelar_sinListaEspera() {
        val reserva = Reserva(id = 10, usuario = usuario, pista = pista, fechaHora = fechaHora,
            estado = EstadoReserva.CONFIRMADA)
        whenever(usuarioRepository.findByEmail("lucia@pistago.com")).thenReturn(usuario)
        whenever(reservaRepository.findById(10)).thenReturn(Optional.of(reserva))
        whenever(reservaRepository.save(any<Reserva>())).thenAnswer { it.arguments[0] as Reserva }
        whenever(listaEsperaRepository.findByPistaIdAndFechaHoraOrderByCreatedAtAsc(1, fechaHora))
            .thenReturn(emptyList())

        reservaService.cancelar("lucia@pistago.com", 10)

        // No se envió ninguna push
        verify(fcmService, never()).enviarNotificacion(any(), any(), any(), any())
        verify(notificacionRepository, never()).save(any())
    }
}