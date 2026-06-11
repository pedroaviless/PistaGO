package me.nacimiento.pistago_backend

import me.nacimiento.pistago_backend.config.FirebaseProperties
import me.nacimiento.pistago_backend.config.UploadsProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(UploadsProperties::class, FirebaseProperties::class)
class PistagoBackendApplication

fun main(args: Array<String>) {
    runApplication<PistagoBackendApplication>(*args)
}