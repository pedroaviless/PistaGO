package me.nacimiento.pistago.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                var token: String? = null
                kotlinx.coroutines.runBlocking {
                    tokenDataStore.token.collect {
                        token = it
                        return@collect
                    }
                }
                token?.let {
                    requestBuilder.addHeader("Authorization", "Bearer $it")
                }
                chain.proceed(requestBuilder.build())
            }
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