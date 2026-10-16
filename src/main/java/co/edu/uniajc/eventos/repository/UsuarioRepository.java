package co.edu.uniajc.eventos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import co.edu.uniajc.eventos.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}