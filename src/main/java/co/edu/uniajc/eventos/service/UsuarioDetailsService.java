package co.edu.uniajc.eventos.service;

import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import co.edu.uniajc.eventos.entity.Usuario;
import co.edu.uniajc.eventos.repository.UsuarioRepository;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario u = usuarioRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        List<SimpleGrantedAuthority> permisos = u.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getCodigo()))
                .toList();
        return User.withUsername(u.getEmail())
                .password(u.getPasswordHash())
                .disabled(!u.isActivo())
                .authorities(permisos)
                .build();
    }

	public UsuarioRepository getUsuarioRepository() {
		return usuarioRepository;
	}
}