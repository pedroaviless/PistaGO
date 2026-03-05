package me.nacimiento.pistago_backend.controller

import me.nacimiento.pistago_backend.dto.PistaRequest
import me.nacimiento.pistago_backend.dto.PistaResponse
import me.nacimiento.pistago_backend.service.PistaService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/pistas")
class PistaController(
    private val pistaService: PistaService
) {

    @GetMapping
    fun getActivas(): ResponseEntity<List<PistaResponse>> =
        ResponseEntity.ok(pistaService.getActivas())

    @GetMapping("/todas")
    fun getAll(): ResponseEntity<List<PistaResponse>> =
        ResponseEntity.ok(pistaService.getAll())

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<PistaResponse> =
        ResponseEntity.ok(pistaService.getById(id))

    @PostMapping
    fun create(@RequestBody request: PistaRequest): ResponseEntity<PistaResponse> =
        ResponseEntity.ok(pistaService.create(request))

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: PistaRequest): ResponseEntity<PistaResponse> =
        ResponseEntity.ok(pistaService.update(id, request))

    @PatchMapping("/{id}/activa")
    fun setActiva(@PathVariable id: Long, @RequestParam activa: Boolean): ResponseEntity<PistaResponse> =
        ResponseEntity.ok(pistaService.setActiva(id, activa))
}