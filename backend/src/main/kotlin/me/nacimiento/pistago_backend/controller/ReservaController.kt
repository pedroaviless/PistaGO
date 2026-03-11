package me.nacimiento.pistago_backend.controller

import me.nacimiento.pistago_backend.dto.ReservaRequest
import me.nacimiento.pistago_backend.dto.ReservaResponse
import me.nacimiento.pistago_backend.service.ReservaService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/reservas")
class ReservaController(
    private val reservaService: ReservaService
) {

    @PostMapping
    fun crear(
        @AuthenticationPrincipal email: String,
        @RequestBody request: ReservaRequest
    ): ResponseEntity<ReservaResponse> =
        ResponseEntity.ok(reservaService.crear(email, request))

    @GetMapping("/mis-reservas")
    fun getMisReservas(
        @AuthenticationPrincipal email: String
    ): ResponseEntity<List<ReservaResponse>> =
        ResponseEntity.ok(reservaService.getMisReservas(email))

    @PatchMapping("/{id}/cancelar")
    fun cancelar(
        @AuthenticationPrincipal email: String,
        @PathVariable id: Long
    ): ResponseEntity<ReservaResponse> =
        ResponseEntity.ok(reservaService.cancelar(email, id))

    @GetMapping("/todas")
    fun getTodas(): ResponseEntity<List<ReservaResponse>> =
        ResponseEntity.ok(reservaService.getTodas())
}