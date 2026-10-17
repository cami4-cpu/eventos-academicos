package co.edu.uniajc.eventos.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import co.edu.uniajc.eventos.dto.AuditoriaResponse;
import co.edu.uniajc.eventos.entity.Auditoria;
import co.edu.uniajc.eventos.repository.AuditoriaRepository;

@Service
public class AuditoriaService {

    private final AuditoriaRepository repository;

    public AuditoriaService(AuditoriaRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(String usuario, String accion, String entidad, Long idEntidad, String detalle) {
        Auditoria a = new Auditoria();
        a.setFecha(LocalDateTime.now());
        a.setUsuario(usuario == null ? "anónimo" : usuario);
        a.setAccion(accion);
        a.setEntidad(entidad);
        a.setIdEntidad(idEntidad);
        a.setDetalle(detalle != null && detalle.length() > 255 ? detalle.substring(0, 255) : detalle);
        repository.save(a);
    }

    @Transactional(readOnly = true)
    public List<AuditoriaResponse> listar(Authentication auth) {
        boolean esAdmin = auth.getAuthorities().stream()
                .anyMatch(x -> x.getAuthority().equals("ROLE_ADMINISTRADOR"));
        if (!esAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo el administrador puede ver el historial");
        }
        return repository.findTop200ByOrderByFechaDesc().stream()
                .map(a -> new AuditoriaResponse(a.getId(), a.getFecha(), a.getUsuario(),
                        a.getAccion(), a.getEntidad(), a.getIdEntidad(), a.getDetalle()))
                .toList();
    }
}