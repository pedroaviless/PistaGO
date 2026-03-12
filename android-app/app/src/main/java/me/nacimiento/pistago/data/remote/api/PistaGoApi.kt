package me.nacimiento.pistago.data.remote.api

import me.nacimiento.pistago.data.remote.dto.AuthResponse
import me.nacimiento.pistago.data.remote.dto.ListaEsperaRequest
import me.nacimiento.pistago.data.remote.dto.ListaEsperaResponse
import me.nacimiento.pistago.data.remote.dto.LoginRequest
import me.nacimiento.pistago.data.remote.dto.PistaAdminRequest
import me.nacimiento.pistago.data.remote.dto.PistaResponse
import me.nacimiento.pistago.data.remote.dto.RegisterRequest
import me.nacimiento.pistago.data.remote.dto.ReservaRequest
import me.nacimiento.pistago.data.remote.dto.ReservaResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT

interface PistaGoApi {

    // Auth
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    // Lista de espera
    @POST("api/lista-espera")
    suspend fun apuntarseListaEspera(@Body request: ListaEsperaRequest): Response<ListaEsperaResponse>

    @GET("api/lista-espera/mi-lista")
    suspend fun getMiListaEspera(): Response<List<ListaEsperaResponse>>

    @DELETE("api/lista-espera/{id}")
    suspend fun salirListaEspera(@retrofit2.http.Path("id") id: Long): Response<Void>
    // Pistas
    @GET("api/pistas")
    suspend fun getPistas(): Response<List<PistaResponse>>

    @GET("api/pistas/{id}")
    suspend fun getPistaById(@retrofit2.http.Path("id") id: Long): Response<PistaResponse>

    // Reservas
    @POST("api/reservas")
    suspend fun crearReserva(@Body request: ReservaRequest): Response<ReservaResponse>

    @GET("api/reservas/mis-reservas")
    suspend fun getMisReservas(): Response<List<ReservaResponse>>

    @PATCH("api/reservas/{id}/cancelar")
    suspend fun cancelarReserva(@retrofit2.http.Path("id") id: Long): Response<ReservaResponse>

    @POST("api/auth/fcm-token")
    suspend fun updateFcmToken(
        @Header("Authorization") authHeader: String,
        @Body body: Map<String, String>
    ): Response<Void>

    // Admin - Pistas
    @GET("api/pistas/todas")
    suspend fun todasLasPistas(): Response<List<PistaResponse>>

    @POST("api/pistas")
    suspend fun crearPista(@Body request: PistaAdminRequest): Response<PistaResponse>

    @PUT("api/pistas/{id}")
    suspend fun actualizarPista(
        @retrofit2.http.Path("id") id: Long,
        @Body request: PistaAdminRequest
    ): Response<PistaResponse>

    @PATCH("api/pistas/{id}/activa")
    suspend fun setActivaPista(
        @retrofit2.http.Path("id") id: Long,
        @retrofit2.http.Query("activa") activa: Boolean
    ): Response<PistaResponse>

    @GET("api/reservas/todas")
    suspend fun getTodasLasReservas(): Response<List<ReservaResponse>>

    @GET("api/reservas/disponibilidad")
    suspend fun getDisponibilidad(
        @retrofit2.http.Query("fecha") fecha: String,
        @retrofit2.http.Query("pistaId") pistaId: Long
    ): Response<List<String>>
}

