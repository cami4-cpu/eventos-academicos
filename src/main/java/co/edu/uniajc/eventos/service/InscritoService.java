package co.edu.uniajc.eventos.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import co.edu.uniajc.eventos.dto.IndicadoresResponse;
import co.edu.uniajc.eventos.dto.InscritoResponse;
import co.edu.uniajc.eventos.entity.EstadoAsistencia;
import co.edu.uniajc.eventos.entity.EstadoInscripcion;
import co.edu.uniajc.eventos.entity.Evento;
import co.edu.uniajc.eventos.entity.Inscripcion;
import co.edu.uniajc.eventos.repository.EventoRepository;
import co.edu.uniajc.eventos.repository.InscripcionRepository;

@Service
public class InscritoService {

    private final EventoRepository eventoRepository;
    private final InscripcionRepository inscripcionRepository;
    private final AuditoriaService auditoria;

    public InscritoService(EventoRepository eventoRepository,
                           InscripcionRepository inscripcionRepository,
                           AuditoriaService auditoria) {
        this.eventoRepository = eventoRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public List<InscritoResponse> listar(Long idEvento, Authentication auth) {
        Evento evento = buscarEvento(idEvento);
        verificarPropietario(evento, auth);
        return inscripcionRepository.findByEventoIdOrderByFechaInscripcionAsc(idEvento).stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public IndicadoresResponse indicadores(Long idEvento, Authentication auth) {
        Evento evento = buscarEvento(idEvento);
        verificarPropietario(evento, auth);

        long cupo = evento.getCupo();
        long activos = inscripcionRepository.countByEventoIdAndEstado(idEvento, EstadoInscripcion.ACTIVA);
        long asistieron = inscripcionRepository.countByEventoIdAndEstadoAndAsistencia(
                idEvento, EstadoInscripcion.ACTIVA, EstadoAsistencia.ASISTIO);
        long noAsistieron = inscripcionRepository.countByEventoIdAndEstadoAndAsistencia(
                idEvento, EstadoInscripcion.ACTIVA, EstadoAsistencia.NO_ASISTIO);
        long pendientes = activos - asistieron - noAsistieron;
        long libres = Math.max(cupo - activos, 0);
        double asistencia = activos == 0 ? 0 : Math.round(asistieron * 1000.0 / activos) / 10.0;
        double ocupacion = cupo == 0 ? 0 : Math.round(activos * 1000.0 / cupo) / 10.0;

        return new IndicadoresResponse(cupo, activos, libres, ocupacion,
                asistieron, noAsistieron, pendientes, asistencia);
    }

    @Transactional
    public InscritoResponse marcarAsistencia(Long idEvento, Long idInscripcion,
                                             EstadoAsistencia valor, Authentication auth) {
        Evento evento = buscarEvento(idEvento);
        verificarPropietario(evento, auth);

        if (evento.getFecha().isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La asistencia se registra desde el día del evento");
        }
        Inscripcion i = inscripcionRepository.findById(idInscripcion)
                .filter(x -> x.getEvento().getId().equals(idEvento))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Inscripción no encontrada en este evento"));
        if (i.getEstado() != EstadoInscripcion.ACTIVA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La inscripción está cancelada");
        }
        i.setAsistencia(valor);
        Inscripcion guardada = inscripcionRepository.save(i);
        auditoria.registrar(auth.getName(), "ASISTENCIA_REGISTRADA", "INSCRIPCION",
                idInscripcion, "Evento " + idEvento + ": " + valor);
        return aRespuesta(guardada);
    }

    private Evento buscarEvento(Long idEvento) {
        return eventoRepository.findById(idEvento)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));
    }

    private void verificarPropietario(Evento evento, Authentication auth) {
        boolean esAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));
        boolean esDueno = evento.getOrganizador().getEmail().equalsIgnoreCase(auth.getName());
        if (!esAdmin && !esDueno) {
            auditoria.registrar(auth.getName(), "ACCESO_NO_AUTORIZADO", "EVENTO",
                    evento.getId(), "Intentó operar sobre un evento ajeno");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este evento no es tuyo");
        }
    }

    private InscritoResponse aRespuesta(Inscripcion i) {
        return new InscritoResponse(i.getId(), i.getUsuario().getNombre(), i.getUsuario().getEmail(),
                i.getEstado(), i.getAsistencia(), i.getFechaInscripcion());
    }
}