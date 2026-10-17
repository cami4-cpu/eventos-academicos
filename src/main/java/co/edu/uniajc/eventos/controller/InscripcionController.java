package co.edu.uniajc.eventos.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import co.edu.uniajc.eventos.dto.InscripcionResponse;
import co.edu.uniajc.eventos.service.InscripcionService;

@RestController
@RequestMapping("/api/inscripciones")
public class InscripcionController {

    private final InscripcionService service;

    public InscripcionController(InscripcionService service) {
        this.service = service;
    }

    @PostMapping("/{idEvento}")
    @ResponseStatus(HttpStatus.CREATED)
    public InscripcionResponse inscribir(@PathVariable Long idEvento, Authentication auth) {
        return service.inscribir(idEvento, auth);
    }

    @GetMapping("/mias")
    public List<InscripcionResponse> mias(Authentication auth) {
        return service.misInscripciones(auth);
    }

    @DeleteMapping("/{idEvento}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelar(@PathVariable Long idEvento, Authentication auth) {
        service.cancelar(idEvento, auth);
    }
}