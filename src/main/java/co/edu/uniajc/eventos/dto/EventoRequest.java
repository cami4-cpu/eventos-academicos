package co.edu.uniajc.eventos.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import co.edu.uniajc.eventos.entity.Modalidad;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EventoRequest(
        @NotBlank String titulo,
        String descripcion,
        @NotNull LocalDate fecha,
        @NotNull LocalTime horaInicio,
        @NotNull LocalTime horaFin,
        @NotNull Modalidad modalidad,
        String ubicacion,
        @Min(1) int cupo,
        @NotNull Long idCategoria,
        @NotNull Long idOrganizador) {
}