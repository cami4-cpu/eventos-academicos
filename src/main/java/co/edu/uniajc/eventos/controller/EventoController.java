package co.edu.uniajc.eventos.controller;
import co.edu.uniajc.eventos.entity.EstadoEvento;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import co.edu.uniajc.eventos.dto.EventoRequest;
import co.edu.uniajc.eventos.dto.EventoResponse;
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
    public EventoResponse crear(@Valid @RequestBody EventoRequest request) {
        return service.crear(request);
    }
    @PutMapping("/{id}")
    public EventoResponse actualizar(@PathVariable Long id, @Valid @RequestBody EventoRequest request) {
        return service.actualizar(id, request);
    }

    @PatchMapping("/{id}/estado")
    public EventoResponse cambiarEstado(@PathVariable Long id, @RequestParam EstadoEvento estado) {
        return service.cambiarEstado(id, estado);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}