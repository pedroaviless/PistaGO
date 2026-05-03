package me.nacimiento.pistago.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import me.nacimiento.pistago.data.local.TokenDataStore
import me.nacimiento.pistago.data.remote.api.PistaGoApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val BASE_URL = "https://api.nacimiento.me/"

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        tokenDataStore: TokenDataStore
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            // 1. Auth interceptor PRIMERO (añade el token)
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val requestBuilder = originalRequest.newBuilder()

                val token = runBlocking {
                    tokenDataStore.token.firstOrNull()
                }

                token?.let {
                    requestBuilder.addHeader("Authorization", "Bearer $it")
                }

                val response = chain.proceed(requestBuilder.build())

                // Si el servidor rechaza por token caducado/inválido, limpiar sesión
                // Excluimos endpoints de auth porque ahí el 401/403 es por credenciales malas,
                // no por token caducado
                val esEndpointAuth = originalRequest.url.encodedPath.startsWith("/api/auth/")
                if ((response.code == 401 || response.code == 403) && !esEndpointAuth && token != null) {
                    android.util.Log.w(
                        "AUTH_INTERCEPTOR",
                        "Token rechazado (${response.code}) en ${originalRequest.url}. Limpiando sesión."
                    )
                    runBlocking {
                        tokenDataStore.clearSession()
                    }
                }

                response
            }
            // 2. Logging interceptor DESPUÉS
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun providePistaGoApi(retrofit: Retrofit): PistaGoApi =
        retrofit.create(PistaGoApi::class.java)
}