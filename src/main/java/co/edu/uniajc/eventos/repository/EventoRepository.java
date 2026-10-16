package co.edu.uniajc.eventos.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import co.edu.uniajc.eventos.entity.EstadoEvento;
import co.edu.uniajc.eventos.entity.Evento;

public interface EventoRepository extends JpaRepository<Evento, Long> {
    List<Evento> findByEstadoOrderByFechaAsc(EstadoEvento estado);
}