package co.edu.uniajc.eventos.dto;

public record IndicadoresResponse(
        long cupo,
        long inscritosActivos,
        long cuposLibres,
        double porcentajeOcupacion,
        long asistieron,
        long noAsistieron,
        long pendientes,
        double porcentajeAsistencia) {
}