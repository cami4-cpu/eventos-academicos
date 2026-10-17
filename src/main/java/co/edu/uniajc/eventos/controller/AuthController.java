package co.edu.uniajc.eventos.controller;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import co.edu.uniajc.eventos.dto.RegistroRequest;
import co.edu.uniajc.eventos.dto.UsuarioResponse;
import co.edu.uniajc.eventos.service.AuthService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registro(@Valid @RequestBody RegistroRequest request) {
        return service.registrar(request);
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication auth) {
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return Map.of(
                "email", auth.getName(),
                "roles", auth.getAuthorities().stream().map(a -> a.getAuthority()).toList());
    }
}