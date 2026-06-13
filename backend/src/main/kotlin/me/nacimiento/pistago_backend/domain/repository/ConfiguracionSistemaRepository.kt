package me.nacimiento.pistago_backend.domain.repository

import me.nacimiento.pistago_backend.domain.entity.ConfiguracionSistema
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


//Este interface está prepara para trabajo futuro

@Repository
interface ConfiguracionSistemaRepository : JpaRepository<ConfiguracionSistema, Long>