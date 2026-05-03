package me.nacimiento.pistago_backend

import me.nacimiento.pistago_backend.config.UploadsProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(UploadsProperties::class)
class PistagoBackendApplication

fun main(args: Array<String>) {
	runApplication<PistagoBackendApplication>(*args)
}
