package co.edu.uniajc.eventos.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import co.edu.uniajc.eventos.dto.RegistroRequest;
import co.edu.uniajc.eventos.dto.UsuarioResponse;
import co.edu.uniajc.eventos.entity.Rol;
import co.edu.uniajc.eventos.entity.Usuario;
import co.edu.uniajc.eventos.repository.RolRepository;
import co.edu.uniajc.eventos.repository.UsuarioRepository;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository, RolRepository rolRepository,
                       PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponse registrar(RegistroRequest r) {
        String email = r.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese correo");
        }
        Rol participante = rolRepository.findByCodigo("PARTICIPANTE")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Falta el rol PARTICIPANTE"));

        Usuario u = new Usuario();
        u.setNombre(r.nombre().trim());
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(r.password()));
        u.setActivo(true);
        u.getRoles().add(participante);
        Usuario guardado = usuarioRepository.save(u);
        return new UsuarioResponse(guardado.getId(), guardado.getNombre(), guardado.getEmail());
    }
}