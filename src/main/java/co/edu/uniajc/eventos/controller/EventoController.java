package co.edu.uniajc.eventos.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import co.edu.uniajc.eventos.dto.EventoRequest;
import co.edu.uniajc.eventos.dto.EventoResponse;
import co.edu.uniajc.eventos.entity.EstadoEvento;
import co.edu.uniajc.eventos.service.EventoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/eventos")
public class EventoController {

    private final EventoService service;

    public EventoController(EventoService service) {
        this.service = service;
    }

    @GetMapping
    public List<EventoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public EventoResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventoResponse crear(@Valid @RequestBody EventoRequest request, Authentication auth) {
        return service.crear(request, auth);
    }

    @PutMapping("/{id}")
    public EventoResponse actualizar(@PathVariable Long id,
                                     @Valid @RequestBody EventoRequest request,
                                     Authentication auth) {
        return service.actualizar(id, request, auth);
    }

    @PatchMapping("/{id}/estado")
    public EventoResponse cambiarEstado(@PathVariable Long id,
                                        @RequestParam EstadoEvento estado,
                                        Authentication auth) {
        return service.cambiarEstado(id, estado, auth);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id, Authentication auth) {
        service.eliminar(id, auth);
    }
}