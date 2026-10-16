package co.edu.uniajc.eventos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import co.edu.uniajc.eventos.entity.CategoriaEvento;

public interface CategoriaEventoRepository extends JpaRepository<CategoriaEvento, Long> {
}