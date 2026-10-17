package co.edu.uniajc.eventos.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import co.edu.uniajc.eventos.entity.Modalidad;

public record EventoCatalogoResponse(
        Long id,
        String titulo,
        String descripcion,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        Modalidad modalidad,
        String ubicacion,
        int cupo,
        int cuposDisponibles,
        boolean inscrito,
        String categoria,
        String organizador) {
}
