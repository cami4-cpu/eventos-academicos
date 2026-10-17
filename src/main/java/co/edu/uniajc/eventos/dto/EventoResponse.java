package co.edu.uniajc.eventos.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import co.edu.uniajc.eventos.entity.EstadoEvento;
import co.edu.uniajc.eventos.entity.Modalidad;

public record EventoResponse(
        Long id,
        String titulo,
        String descripcion,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        Modalidad modalidad,
        String ubicacion,
        int cupo,
        EstadoEvento estado,
        String categoria,
        String organizador,
        String motivoCancelacion,
        LocalDateTime fechaCancelacion,
        String canceladoPor) {
}