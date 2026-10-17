package co.edu.uniajc.eventos.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import co.edu.uniajc.eventos.entity.EstadoInscripcion;

public record InscripcionResponse(
        Long id,
        Long idEvento,
        String titulo,
        LocalDate fecha,
        LocalTime horaInicio,
        String ubicacion,
        EstadoInscripcion estado,
        LocalDateTime fechaInscripcion) {
}