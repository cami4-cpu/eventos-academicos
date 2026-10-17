package co.edu.uniajc.eventos.dto;

import java.time.LocalDateTime;
import co.edu.uniajc.eventos.entity.EstadoAsistencia;
import co.edu.uniajc.eventos.entity.EstadoInscripcion;

public record InscritoResponse(
        Long idInscripcion,
        String nombre,
        String email,
        EstadoInscripcion estado,
        EstadoAsistencia asistencia,
        LocalDateTime fechaInscripcion) {
}