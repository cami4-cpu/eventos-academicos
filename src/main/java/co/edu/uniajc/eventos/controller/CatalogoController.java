package co.edu.uniajc.eventos.controller;

import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import co.edu.uniajc.eventos.dto.EventoResponse;
import co.edu.uniajc.eventos.entity.Modalidad;
import co.edu.uniajc.eventos.repository.CategoriaEventoRepository;
import co.edu.uniajc.eventos.service.EventoService;

@RestController
@RequestMapping("/api")
public class CatalogoController {

    public record CategoriaDto(Long id, String nombre) {
    }

    private final EventoService service;
    private final CategoriaEventoRepository categoriaRepository;

    public CatalogoController(EventoService service, CategoriaEventoRepository categoriaRepository) {
        this.service = service;
        this.categoriaRepository = categoriaRepository;
    }

    @GetMapping("/catalogo")
    public List<EventoResponse> catalogo(
            @RequestParam(required = false) Long categoria,
            @RequestParam(required = false) Modalidad modalidad,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) String q) {
        return service.catalogo(categoria, modalidad, fecha, q);
    }

    @GetMapping("/categorias")
    public List<CategoriaDto> categorias() {
        return categoriaRepository.findAll().stream()
                .map(c -> new CategoriaDto(c.getId(), c.getNombre()))
                .toList();
    }
}