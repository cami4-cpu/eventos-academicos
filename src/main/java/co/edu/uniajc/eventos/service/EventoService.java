package co.edu.uniajc.eventos.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import co.edu.uniajc.eventos.dto.EventoCatalogoResponse;
import co.edu.uniajc.eventos.dto.EventoRequest;
import co.edu.uniajc.eventos.dto.EventoResponse;
import co.edu.uniajc.eventos.entity.CategoriaEvento;
import co.edu.uniajc.eventos.entity.EstadoEvento;
import co.edu.uniajc.eventos.entity.EstadoInscripcion;
import co.edu.uniajc.eventos.entity.Evento;
import co.edu.uniajc.eventos.entity.Modalidad;
import co.edu.uniajc.eventos.entity.Usuario;
import co.edu.uniajc.eventos.repository.CategoriaEventoRepository;
import co.edu.uniajc.eventos.repository.EventoRepository;
import co.edu.uniajc.eventos.repository.InscripcionRepository;
import co.edu.uniajc.eventos.repository.UsuarioRepository;

@Service
public class EventoService {

    private final EventoRepository eventoRepository;
    private final CategoriaEventoRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final InscripcionRepository inscripcionRepository;
    private final AuditoriaService auditoria;

    public EventoService(EventoRepository eventoRepository,
                         CategoriaEventoRepository categoriaRepository,
                         UsuarioRepository usuarioRepository,
                         InscripcionRepository inscripcionRepository,
                         AuditoriaService auditoria) {
        this.eventoRepository = eventoRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public List<EventoResponse> listar() {
        return eventoRepository.findAll().stream().map(this::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public EventoResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    @Transactional
    public EventoResponse crear(EventoRequest r, Authentication auth) {
        validarHoras(r);
        CategoriaEvento categoria = buscarCategoria(r.idCategoria());
        Usuario organizador = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no válido"));

        Evento e = new Evento();
        copiarDatos(e, r, categoria);
        e.setOrganizador(organizador);
        Evento guardado = eventoRepository.save(e);
        auditoria.registrar(auth.getName(), "EVENTO_CREADO", "EVENTO", guardado.getId(), guardado.getTitulo());
        return aRespuesta(guardado);
    }

    @Transactional
    public EventoResponse actualizar(Long id, EventoRequest r, Authentication auth) {
        validarHoras(r);
        Evento e = buscar(id);
        verificarPropietario(e, auth);
        if (e.getEstado() == EstadoEvento.CANCELADO || e.getEstado() == EstadoEvento.FINALIZADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Un evento cancelado o finalizado no se puede modificar");
        }
        long activos = inscripcionRepository.countByEventoIdAndEstado(id, EstadoInscripcion.ACTIVA);
        if (r.cupo() < activos) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El cupo no puede ser menor que los inscritos activos (" + activos + ")");
        }
        copiarDatos(e, r, buscarCategoria(r.idCategoria()));
        Evento guardado = eventoRepository.save(e);
        auditoria.registrar(auth.getName(), "EVENTO_EDITADO", "EVENTO", id, guardado.getTitulo());
        return aRespuesta(guardado);
    }

    @Transactional
    public EventoResponse cambiarEstado(Long id, EstadoEvento nuevoEstado, Authentication auth) {
        Evento e = buscar(id);
        verificarPropietario(e, auth);

        if (nuevoEstado == EstadoEvento.CANCELADO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Para cancelar un evento debes indicar el motivo");
        }
        EstadoEvento actual = e.getEstado();
        if (nuevoEstado == EstadoEvento.FINALIZADO && e.getFecha().isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El evento solo se puede finalizar desde el día en que se realiza");
        }
        boolean permitido = switch (nuevoEstado) {
            case PUBLICADO -> actual == EstadoEvento.BORRADOR || actual == EstadoEvento.CERRADO;
            case CERRADO -> actual == EstadoEvento.PUBLICADO;
            case FINALIZADO -> actual == EstadoEvento.PUBLICADO || actual == EstadoEvento.CERRADO;
            default -> false;
        };
        if (!permitido) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede pasar el evento de " + actual + " a " + nuevoEstado);
        }
        e.setEstado(nuevoEstado);
        Evento guardado = eventoRepository.save(e);
        auditoria.registrar(auth.getName(), "EVENTO_" + nuevoEstado.name(), "EVENTO", id,
                "De " + actual + " a " + nuevoEstado);
        return aRespuesta(guardado);
    }

    @Transactional
    public EventoResponse cancelar(Long id, String motivo, Authentication auth) {
        Evento e = buscar(id);
        verificarPropietario(e, auth);
        if (e.getEstado() == EstadoEvento.CANCELADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El evento ya está cancelado");
        }
        if (e.getEstado() == EstadoEvento.FINALIZADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Un evento finalizado no se puede cancelar");
        }
        e.setEstado(EstadoEvento.CANCELADO);
        e.setMotivoCancelacion(motivo.trim());
        e.setFechaCancelacion(LocalDateTime.now());
        e.setCanceladoPor(auth.getName());
        Evento guardado = eventoRepository.save(e);
        auditoria.registrar(auth.getName(), "EVENTO_CANCELADO", "EVENTO", id, "Motivo: " + motivo.trim());
        return aRespuesta(guardado);
    }

    @Transactional
    public void eliminar(Long id, Authentication auth) {
        Evento e = buscar(id);
        verificarPropietario(e, auth);
        if (inscripcionRepository.countByEventoId(id) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El evento tiene inscripciones y no se puede eliminar; cancélalo para conservar el historial");
        }
        String titulo = e.getTitulo();
        eventoRepository.delete(e);
        auditoria.registrar(auth.getName(), "EVENTO_ELIMINADO", "EVENTO", id, titulo);
    }

    @Transactional(readOnly = true)
    public List<EventoCatalogoResponse> catalogo(Long idCategoria, Modalidad modalidad,
                                                 LocalDate fecha, String q, Authentication auth) {
        Long idUsuario = idUsuarioActual(auth);
        return eventoRepository.findByEstadoOrderByFechaAsc(EstadoEvento.PUBLICADO).stream()
                .filter(e -> idCategoria == null || e.getCategoria().getId().equals(idCategoria))
                .filter(e -> modalidad == null || e.getModalidad() == modalidad)
                .filter(e -> fecha == null || e.getFecha().equals(fecha))
                .filter(e -> q == null || q.isBlank()
                        || e.getTitulo().toLowerCase().contains(q.toLowerCase()))
                .map(e -> aCatalogo(e, idUsuario))
                .toList();
    }

    private Long idUsuarioActual(Authentication auth) {
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return usuarioRepository.findByEmail(auth.getName()).map(Usuario::getId).orElse(null);
    }

    private EventoCatalogoResponse aCatalogo(Evento e, Long idUsuario) {
        long ocupados = inscripcionRepository.countByEventoIdAndEstado(e.getId(), EstadoInscripcion.ACTIVA);
        int disponibles = (int) Math.max(0, e.getCupo() - ocupados);
        boolean inscrito = idUsuario != null && inscripcionRepository
                .existsByEventoIdAndUsuarioIdAndEstado(e.getId(), idUsuario, EstadoInscripcion.ACTIVA);
        return new EventoCatalogoResponse(e.getId(), e.getTitulo(), e.getDescripcion(), e.getFecha(),
                e.getHoraInicio(), e.getHoraFin(), e.getModalidad(), e.getUbicacion(), e.getCupo(),
                disponibles, inscrito, e.getCategoria().getNombre(), e.getOrganizador().getNombre());
    }

    private void validarHoras(EventoRequest r) {
        if (!r.horaFin().isAfter(r.horaInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La hora de fin debe ser posterior a la de inicio");
        }
    }

    private CategoriaEvento buscarCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no existe"));
    }

    private void copiarDatos(Evento e, EventoRequest r, CategoriaEvento categoria) {
        e.setTitulo(r.titulo());
        e.setDescripcion(r.descripcion());
        e.setFecha(r.fecha());
        e.setHoraInicio(r.horaInicio());
        e.setHoraFin(r.horaFin());
        e.setModalidad(r.modalidad());
        e.setUbicacion(r.ubicacion());
        e.setCupo(r.cupo());
        e.setCategoria(categoria);
    }

    private void verificarPropietario(Evento e, Authentication auth) {
        boolean esAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));
        if (!esAdmin && !e.getOrganizador().getEmail().equalsIgnoreCase(auth.getName())) {
            auditoria.registrar(auth.getName(), "ACCESO_NO_AUTORIZADO", "EVENTO", e.getId(),
                    "Intentó modificar un evento ajeno");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No puedes modificar eventos de otro organizador");
        }
    }

    private Evento buscar(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));
    }

    private EventoResponse aRespuesta(Evento e) {
        return new EventoResponse(e.getId(), e.getTitulo(), e.getDescripcion(), e.getFecha(),
                e.getHoraInicio(), e.getHoraFin(), e.getModalidad(), e.getUbicacion(), e.getCupo(),
                e.getEstado(), e.getCategoria().getNombre(), e.getOrganizador().getNombre(),
                e.getMotivoCancelacion(), e.getFechaCancelacion(), e.getCanceladoPor());
    }
}
