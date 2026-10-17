package co.edu.uniajc.eventos.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import co.edu.uniajc.eventos.dto.EventoRequest;
import co.edu.uniajc.eventos.dto.EventoResponse;
import co.edu.uniajc.eventos.entity.CategoriaEvento;
import co.edu.uniajc.eventos.entity.EstadoEvento;
import co.edu.uniajc.eventos.entity.Evento;
import co.edu.uniajc.eventos.entity.Modalidad;
import co.edu.uniajc.eventos.entity.Usuario;
import co.edu.uniajc.eventos.repository.CategoriaEventoRepository;
import co.edu.uniajc.eventos.repository.EventoRepository;
import co.edu.uniajc.eventos.repository.UsuarioRepository;

@Service
public class EventoService {

    private final EventoRepository eventoRepository;
    private final CategoriaEventoRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;

    public EventoService(EventoRepository eventoRepository,
                         CategoriaEventoRepository categoriaRepository,
                         UsuarioRepository usuarioRepository) {
        this.eventoRepository = eventoRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
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
        return aRespuesta(eventoRepository.save(e));
    }

    @Transactional
    public EventoResponse actualizar(Long id, EventoRequest r, Authentication auth) {
        validarHoras(r);
        Evento e = buscar(id);
        verificarPropietario(e, auth);
        copiarDatos(e, r, buscarCategoria(r.idCategoria()));
        return aRespuesta(eventoRepository.save(e));
    }

    @Transactional
    public EventoResponse cambiarEstado(Long id, EstadoEvento nuevoEstado, Authentication auth) {
        Evento e = buscar(id);
        verificarPropietario(e, auth);
        e.setEstado(nuevoEstado);
        return aRespuesta(eventoRepository.save(e));
    }

    @Transactional
    public void eliminar(Long id, Authentication auth) {
        Evento e = buscar(id);
        verificarPropietario(e, auth);
        eventoRepository.delete(e);
    }

    @Transactional(readOnly = true)
    public List<EventoResponse> catalogo(Long idCategoria, Modalidad modalidad, LocalDate fecha, String q) {
        return eventoRepository.findByEstadoOrderByFechaAsc(EstadoEvento.PUBLICADO).stream()
                .filter(e -> idCategoria == null || e.getCategoria().getId().equals(idCategoria))
                .filter(e -> modalidad == null || e.getModalidad() == modalidad)
                .filter(e -> fecha == null || e.getFecha().equals(fecha))
                .filter(e -> q == null || q.isBlank()
                        || e.getTitulo().toLowerCase().contains(q.toLowerCase()))
                .map(this::aRespuesta)
                .toList();
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
        if (!esAdmin && !e.getOrganizador().getEmail().equals(auth.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No puedes modificar eventos de otro organizador");
        }
    }

    public EventoRepository getEventoRepository() {
		return eventoRepository;
	}

	public CategoriaEventoRepository getCategoriaRepository() {
		return categoriaRepository;
	}

	public UsuarioRepository getUsuarioRepository() {
		return usuarioRepository;
	}

	private Evento buscar(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));
    }

    private EventoResponse aRespuesta(Evento e) {
        return new EventoResponse(e.getId(), e.getTitulo(), e.getDescripcion(), e.getFecha(),
                e.getHoraInicio(), e.getHoraFin(), e.getModalidad(), e.getUbicacion(), e.getCupo(),
                e.getEstado(), e.getCategoria().getNombre(), e.getOrganizador().getNombre());
    }
}