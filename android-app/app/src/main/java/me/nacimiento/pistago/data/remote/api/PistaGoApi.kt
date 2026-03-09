package me.nacimiento.pistago.data.remote.api

import me.nacimiento.pistago.data.remote.dto.AuthResponse
import me.nacimiento.pistago.data.remote.dto.LoginRequest
import me.nacimiento.pistago.data.remote.dto.PistaResponse
import me.nacimiento.pistago.data.remote.dto.RegisterRequest
import me.nacimiento.pistago.data.remote.dto.ReservaRequest
import me.nacimiento.pistago.data.remote.dto.ReservaResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST

interface PistaGoApi {

    // Auth
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

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
}