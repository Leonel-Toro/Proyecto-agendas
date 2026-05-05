package com.calendario.agendarreservas.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistorialPacienteDTO {

    private Long idHistorial;

    // Reserva vinculada (requerida al crear)
    private Long idReserva;

    // Datos enriquecidos del paciente para respuesta
    private Long pacienteId;
    private String pacienteNombre;
    private String pacienteRut;

    // Datos enriquecidos del psicólogo para respuesta
    private Long psicologoId;
    private String psicologoNombre;

    // Datos clínicos
    private String motivoConsulta;
    private String tipoSesion;
    private Boolean crisis = false;
    private Boolean alta = false;
    private Boolean posibleAbandono = false;
    private String notasGenerales;

    // Notas de sesión
    private List<NotasSesionDTO> notasSesion;

    // Auditoría
    private Instant fechaCreacion;
    private Instant fechaActualizacion;
}
