package co.edu.uniajc.eventos.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import co.edu.uniajc.eventos.entity.EstadoEvento;
import co.edu.uniajc.eventos.entity.Evento;
import jakarta.persistence.LockModeType;

public interface EventoRepository extends JpaRepository<Evento, Long> {

    List<Evento> findByEstadoOrderByFechaAsc(EstadoEvento estado);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Evento e where e.id = :id")
    Optional<Evento> findByIdForUpdate(@Param("id") Long id);
}