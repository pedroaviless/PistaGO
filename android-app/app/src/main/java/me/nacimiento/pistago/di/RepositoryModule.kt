package me.nacimiento.pistago.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.nacimiento.pistago.data.repository.AuthRepositoryImpl
import me.nacimiento.pistago.data.repository.ListaEsperaRepositoryImpl
import me.nacimiento.pistago.data.repository.PistaRepositoryImpl
import me.nacimiento.pistago.data.repository.ReservaRepositoryImpl
import me.nacimiento.pistago.domain.repository.AuthRepository
import me.nacimiento.pistago.domain.repository.ListaEsperaRepository
import me.nacimiento.pistago.domain.repository.PistaRepository
import me.nacimiento.pistago.domain.repository.ReservaRepository
import javax.inject.Singleton
import me.nacimiento.pistago.data.repository.EstadisticasRepositoryImpl
import me.nacimiento.pistago.domain.repository.EstadisticasRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindPistaRepository(impl: PistaRepositoryImpl): PistaRepository

    @Binds
    @Singleton
    abstract fun bindReservaRepository(impl: ReservaRepositoryImpl): ReservaRepository

    @Binds
    @Singleton
    abstract fun bindListaEsperaRepository(impl: ListaEsperaRepositoryImpl): ListaEsperaRepository

    @Binds
    abstract fun bindEstadisticasRepository(impl: EstadisticasRepositoryImpl): EstadisticasRepository
}