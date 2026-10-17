package co.edu.uniajc.eventos.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import co.edu.uniajc.eventos.entity.Auditoria;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
    List<Auditoria> findTop200ByOrderByFechaDesc();
}