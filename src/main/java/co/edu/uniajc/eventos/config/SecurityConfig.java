package co.edu.uniajc.eventos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(a -> a
                .requestMatchers("/", "/index.html", "/login.html", "/registro.html",
                        "/css/**", "/js/**", "/favicon.ico",
                        "/api/catalogo", "/api/categorias", "/api/auth/registro").permitAll()
                .requestMatchers("/organizador.html", "/api/eventos/**")
                        .hasAnyRole("ORGANIZADOR", "ADMINISTRADOR")
                .anyRequest().authenticated())
            .formLogin(f -> f
                .loginPage("/login.html")
                .loginProcessingUrl("/api/auth/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler((req, res, auth) -> res.setStatus(200))
                .failureHandler((req, res, ex) -> res.setStatus(401))
                .permitAll())
            .logout(l -> l
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler((req, res, auth) -> res.setStatus(200)));
        return http.build();
    }
}