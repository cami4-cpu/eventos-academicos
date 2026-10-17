package co.edu.uniajc.eventos.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import co.edu.uniajc.eventos.dto.InscripcionResponse;
import co.edu.uniajc.eventos.entity.EstadoEvento;
import co.edu.uniajc.eventos.entity.EstadoInscripcion;
import co.edu.uniajc.eventos.entity.Evento;
import co.edu.uniajc.eventos.entity.Inscripcion;
import co.edu.uniajc.eventos.entity.Usuario;
import co.edu.uniajc.eventos.repository.EventoRepository;
import co.edu.uniajc.eventos.repository.InscripcionRepository;
import co.edu.uniajc.eventos.repository.UsuarioRepository;

@Service
public class InscripcionService {

    private final InscripcionRepository inscripcionRepository;
    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoria;

    public InscripcionService(InscripcionRepository inscripcionRepository,
                              EventoRepository eventoRepository,
                              UsuarioRepository usuarioRepository,
                              AuditoriaService auditoria) {
        this.inscripcionRepository = inscripcionRepository;
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoria = auditoria;
    }

    @Transactional
    public InscripcionResponse inscribir(Long idEvento, Authentication auth) {
        Usuario usuario = usuarioActual(auth);
        Evento evento = eventoRepository.findByIdForUpdate(idEvento)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));

        if (evento.getEstado() != EstadoEvento.PUBLICADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El evento no está abierto a inscripciones");
        }
        LocalDate hoy = LocalDate.now();
        if (evento.getFecha().isBefore(hoy)
                || (evento.getFecha().isEqual(hoy) && !LocalTime.now().isBefore(evento.getHoraInicio()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Las inscripciones están cerradas: el evento ya inició");
        }

        Inscripcion existente = inscripcionRepository
                .findByEventoIdAndUsuarioId(idEvento, usuario.getId()).orElse(null);
        if (existente != null && existente.getEstado() == EstadoInscripcion.ACTIVA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya estás inscrito en este evento");
        }

        long ocupados = inscripcionRepository.countByEventoIdAndEstado(idEvento, EstadoInscripcion.ACTIVA);
        if (ocupados >= evento.getCupo()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No hay cupos disponibles");
        }

        Inscripcion i = existente != null ? existente : new Inscripcion();
        i.setEvento(evento);
        i.setUsuario(usuario);
        i.setEstado(EstadoInscripcion.ACTIVA);
        i.setFechaInscripcion(LocalDateTime.now());
        Inscripcion guardada = inscripcionRepository.save(i);
        auditoria.registrar(usuario.getEmail(), "INSCRIPCION_CREADA", "INSCRIPCION",
                guardada.getId(), "Evento " + idEvento);
        return aRespuesta(guardada);
    }

    @Transactional
    public void cancelar(Long idEvento, Authentication auth) {
        Usuario usuario = usuarioActual(auth);
        Inscripcion i = inscripcionRepository.findByEventoIdAndUsuarioId(idEvento, usuario.getId())
                .filter(x -> x.getEstado() == EstadoInscripcion.ACTIVA)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No tienes una inscripción activa"));
        i.setEstado(EstadoInscripcion.CANCELADA);
        inscripcionRepository.save(i);
        auditoria.registrar(usuario.getEmail(), "INSCRIPCION_CANCELADA", "INSCRIPCION",
                i.getId(), "Evento " + idEvento);
    }

    @Transactional(readOnly = true)
    public List<InscripcionResponse> misInscripciones(Authentication auth) {
        Usuario usuario = usuarioActual(auth);
        return inscripcionRepository.findByUsuarioIdOrderByFechaInscripcionDesc(usuario.getId())
                .stream().map(this::aRespuesta).toList();
    }

    private Usuario usuarioActual(Authentication auth) {
        return usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no válido"));
    }

    private InscripcionResponse aRespuesta(Inscripcion i) {
        Evento e = i.getEvento();
        return new InscripcionResponse(i.getId(), e.getId(), e.getTitulo(), e.getFecha(),
                e.getHoraInicio(), e.getUbicacion(), i.getEstado(), i.getFechaInscripcion());
    }
}