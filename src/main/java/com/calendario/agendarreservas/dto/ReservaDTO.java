package com.calendario.agendarreservas.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservaDTO {

    private Long id;

    // Paciente y psicólogo
    private Long pacienteId;
    private Long psicologoId;

    // Datos enriquecidos para respuesta
    private String pacienteNombre;
    private String pacienteRut;
    private String psicologoNombre;

    // Detalles de la sesión
    private String motivoConsulta;

    @NotBlank(message = "La modalidad es obligatoria (PRESENCIAL o VIRTUAL)")
    private String modalidad;

    @Min(value = 30, message = "La duración mínima es 30 minutos")
    private Integer duracionMinutos = 60;

    @NotNull(message = "La fecha de reserva es obligatoria")
    private Timestamp fechaReserva;

    private Timestamp fechaTermino;

    // Información financiera
    @NotNull(message = "El precio es obligatorio")
    @Min(value = 0, message = "El precio no puede ser negativo")
    private Long precio;

    @Min(value = 0, message = "El monto abonado no puede ser negativo")
    private Long abonado = 0L;

    // Estado (PENDIENTE, CONFIRMADA, CANCELADA, COMPLETADA)
    private String estado;

    // Auditoría
    private Instant fechaCreacion;
    private Instant fechaModificacion;
}
