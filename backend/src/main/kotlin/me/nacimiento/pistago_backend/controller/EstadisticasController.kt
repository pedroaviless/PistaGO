package me.nacimiento.pistago_backend.controller

import me.nacimiento.pistago_backend.dto.EstadisticasResponse
import me.nacimiento.pistago_backend.service.EstadisticasService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/estadisticas")
class EstadisticasController(
    private val estadisticasService: EstadisticasService
) {
    @GetMapping
    fun getEstadisticas(): ResponseEntity<EstadisticasResponse> =
        ResponseEntity.ok(estadisticasService.getEstadisticas())
}