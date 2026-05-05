package com.calendario.agendarreservas.service;

import com.calendario.agendarreservas.dto.ReservaDTO;

import java.util.List;

public interface ReservaService {

    // Paciente
    ReservaDTO crearReserva(ReservaDTO dto);
    List<ReservaDTO> obtenerMisReservas();
    ReservaDTO obtenerMiReserva(Long id);
    ReservaDTO editarReserva(Long id, ReservaDTO dto);
    void cancelarReserva(Long id);

    // Admin
    ReservaDTO crearReservaAdmin(ReservaDTO dto);
    List<ReservaDTO> obtenerTodasReservas();
    List<ReservaDTO> obtenerReservasPorPaciente(Long pacienteId);
    ReservaDTO obtenerReservaAdmin(Long id);
    ReservaDTO editarReservaAdmin(Long id, ReservaDTO dto);
    void cancelarReservaAdmin(Long id);
    ReservaDTO completarReserva(Long id);
}
