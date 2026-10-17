package co.edu.uniajc.eventos.dto;

import java.time.LocalDateTime;

public record AuditoriaResponse(
        Long id,
        LocalDateTime fecha,
        String usuario,
        String accion,
        String entidad,
        Long idEntidad,
        String detalle) {
}