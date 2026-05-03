package me.nacimiento.pistago_backend.controller

import jakarta.validation.Valid
import me.nacimiento.pistago_backend.dto.ListaEsperaRequest
import me.nacimiento.pistago_backend.dto.ListaEsperaResponse
import me.nacimiento.pistago_backend.service.ListaEsperaService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/lista-espera")
class ListaEsperaController(
    private val listaEsperaService: ListaEsperaService
) {

    @PostMapping
    fun apuntarse(
        @AuthenticationPrincipal email: String,
        @Valid @RequestBody request: ListaEsperaRequest
    ): ResponseEntity<ListaEsperaResponse> =
        ResponseEntity.ok(listaEsperaService.apuntarse(email, request))

    @GetMapping("/mi-lista")
    fun getMiLista(
        @AuthenticationPrincipal email: String
    ): ResponseEntity<List<ListaEsperaResponse>> =
        ResponseEntity.ok(listaEsperaService.getMiLista(email))

    @DeleteMapping("/{id}")
    fun salir(
        @AuthenticationPrincipal email: String,
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        listaEsperaService.salir(email, id)
        return ResponseEntity.noContent().build()
    }
}