package com.calendario.agendarreservas.service;

import com.calendario.agendarreservas.dto.HistorialPacienteDTO;
import com.calendario.agendarreservas.dto.NotasSesionDTO;

import java.time.Instant;
import java.util.List;

public interface HistorialPacienteService {

    // Admin / Psicólogo
    HistorialPacienteDTO crearHistorial(HistorialPacienteDTO dto);
    HistorialPacienteDTO actualizarHistorial(Long id, HistorialPacienteDTO dto);
    HistorialPacienteDTO obtenerHistorialAdmin(Long id);
    List<HistorialPacienteDTO> obtenerHistorialPorPaciente(Long pacienteId);
    List<HistorialPacienteDTO> obtenerHistorialPorPacienteYRango(Long pacienteId, Instant desde, Instant hasta);
    NotasSesionDTO agregarNota(Long idHistorial, NotasSesionDTO dto);
    void eliminarNota(Long idNota);

    // Paciente
    HistorialPacienteDTO obtenerMiHistorial(Long id);
    List<HistorialPacienteDTO> obtenerMiHistorialCompleto();
}
