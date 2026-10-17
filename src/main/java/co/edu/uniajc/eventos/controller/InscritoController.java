package co.edu.uniajc.eventos.controller;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import co.edu.uniajc.eventos.dto.InscritoResponse;
import co.edu.uniajc.eventos.service.InscritoService;

@RestController
@RequestMapping("/api/eventos/{idEvento}/inscritos")
public class InscritoController {

    private final InscritoService service;

    public InscritoController(InscritoService service) {
        this.service = service;
    }

    @GetMapping
    public List<InscritoResponse> listar(@PathVariable Long idEvento, Authentication auth) {
        return service.listar(idEvento, auth);
    }
}