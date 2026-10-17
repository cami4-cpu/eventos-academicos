package co.edu.uniajc.eventos.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import co.edu.uniajc.eventos.entity.EstadoInscripcion;
import co.edu.uniajc.eventos.entity.Inscripcion;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {

    Optional<Inscripcion> findByEventoIdAndUsuarioId(Long idEvento, Long idUsuario);

    long countByEventoIdAndEstado(Long idEvento, EstadoInscripcion estado);

    List<Inscripcion> findByUsuarioIdOrderByFechaInscripcionDesc(Long idUsuario);
}