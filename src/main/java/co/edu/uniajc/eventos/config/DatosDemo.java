package co.edu.uniajc.eventos.config;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import co.edu.uniajc.eventos.entity.CategoriaEvento;
import co.edu.uniajc.eventos.entity.EstadoEvento;
import co.edu.uniajc.eventos.entity.Evento;
import co.edu.uniajc.eventos.entity.Modalidad;
import co.edu.uniajc.eventos.entity.Rol;
import co.edu.uniajc.eventos.entity.Usuario;
import co.edu.uniajc.eventos.repository.CategoriaEventoRepository;
import co.edu.uniajc.eventos.repository.EventoRepository;
import co.edu.uniajc.eventos.repository.RolRepository;
import co.edu.uniajc.eventos.repository.UsuarioRepository;

/**
 * Siembra datos de demostracion al arrancar: roles, tres usuarios de prueba
 * y dos eventos publicados con fecha futura (siempre hay eventos abiertos).
 */
@Component
public class DatosDemo implements CommandLineRunner {

    private static final String CLAVE_DEMO = "Demo1234!";

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaEventoRepository categoriaRepository;
    private final EventoRepository eventoRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean reiniciarClaves;

    public DatosDemo(RolRepository rolRepository,
                     UsuarioRepository usuarioRepository,
                     CategoriaEventoRepository categoriaRepository,
                     EventoRepository eventoRepository,
                     PasswordEncoder passwordEncoder,
                     @Value("${app.demo.reiniciar-claves:false}") boolean reiniciarClaves) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.eventoRepository = eventoRepository;
        this.passwordEncoder = passwordEncoder;
        this.reiniciarClaves = reiniciarClaves;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Rol rolAdmin = obtenerRol("ADMINISTRADOR", "Administrador");
        Rol rolOrganizador = obtenerRol("ORGANIZADOR", "Organizador");
        Rol rolParticipante = obtenerRol("PARTICIPANTE", "Participante");

        crearUsuario("admin@demo.com", "Administrador Demo", rolAdmin);
        Usuario organizador = crearUsuario("organizador@demo.com", "Organizador Demo", rolOrganizador);
        crearUsuario("participante@demo.com", "Participante Demo", rolParticipante);

        if (reiniciarClaves) {
            // Deja TODAS las cuentas activas y con la clave de demostracion.
            String hash = passwordEncoder.encode(CLAVE_DEMO);
            for (Usuario u : usuarioRepository.findAll()) {
                u.setPasswordHash(hash);
                u.setActivo(true);
                usuarioRepository.save(u);
            }
        }

        CategoriaEvento categoria = categoriaRepository.findAll().stream().findFirst()
                .orElseGet(() -> {
                    CategoriaEvento c = new CategoriaEvento();
                    c.setNombre("Seminario");
                    c.setActiva(true);
                    return categoriaRepository.save(c);
                });

        LocalDate hoy = LocalDate.now();
        asegurarEvento("Taller demo: introducción a Spring Boot", "Taller práctico de demostración.",
                hoy.plusDays(7), LocalTime.of(9, 0), LocalTime.of(11, 0),
                Modalidad.PRESENCIAL, "Aula 301", 30, organizador, categoria);
        asegurarEvento("Charla demo: bases de datos", "Charla virtual de demostración.",
                hoy.plusDays(14), LocalTime.of(14, 0), LocalTime.of(16, 0),
                Modalidad.VIRTUAL, "https://meet.ejemplo.com/demo", 50, organizador, categoria);
    }

    private Rol obtenerRol(String codigo, String nombre) {
        return rolRepository.findByCodigo(codigo).orElseGet(() -> {
            Rol r = new Rol();
            r.setCodigo(codigo);
            r.setNombre(nombre);
            return rolRepository.save(r);
        });
    }

    private Usuario crearUsuario(String email, String nombre, Rol rol) {
        return usuarioRepository.findByEmail(email).orElseGet(() -> {
            Usuario u = new Usuario();
            u.setEmail(email);
            u.setNombre(nombre);
            u.setPasswordHash(passwordEncoder.encode(CLAVE_DEMO));
            u.setActivo(true);
            u.setRoles(new HashSet<>(List.of(rol)));
            return usuarioRepository.save(u);
        });
    }

    /**
     * Si el evento de demostracion no existe, lo crea. Si existe pero ya paso o no esta
     * publicado, lo vuelve a publicar con la fecha nueva. Si sigue abierto, no lo toca.
     */
    private void asegurarEvento(String titulo, String descripcion, LocalDate fecha,
                                LocalTime inicio, LocalTime fin, Modalidad modalidad,
                                String ubicacion, int cupo, Usuario organizador, CategoriaEvento categoria) {
        LocalDate hoy = LocalDate.now();
        Evento e = eventoRepository.findAll().stream()
                .filter(x -> titulo.equals(x.getTitulo()))
                .findFirst()
                .orElse(null);

        if (e != null && e.getEstado() == EstadoEvento.PUBLICADO && !e.getFecha().isBefore(hoy)) {
            return;
        }
        if (e == null) {
            e = new Evento();
            e.setTitulo(titulo);
            e.setOrganizador(organizador);
            e.setCategoria(categoria);
        }
        e.setDescripcion(descripcion);
        e.setFecha(fecha);
        e.setHoraInicio(inicio);
        e.setHoraFin(fin);
        e.setModalidad(modalidad);
        e.setUbicacion(ubicacion);
        e.setCupo(cupo);
        e.setEstado(EstadoEvento.PUBLICADO);
        e.setMotivoCancelacion(null);
        e.setFechaCancelacion(null);
        e.setCanceladoPor(null);
        eventoRepository.save(e);
    }
}