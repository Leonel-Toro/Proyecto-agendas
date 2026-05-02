package com.calendario.agendarreservas.service;

import com.calendario.agendarreservas.dto.ReservaClienteDTO;

import java.util.List;

public interface ReservaService {

    ReservaClienteDTO agendarCliente(ReservaClienteDTO reservaClienteDTO);

    List<ReservaClienteDTO> obtenerHistorial();

    List<ReservaClienteDTO> obtenerHistorialPorUsuario(Long userId);

    List<ReservaClienteDTO> obtenerHistorialPorEmail(String email);

    ReservaClienteDTO obtenerDetalleReserva(Long id);

    ReservaClienteDTO obtenerDetalleReservaAdmin(Long id);

    ReservaClienteDTO editarReserva(ReservaClienteDTO reservaClienteDTO);

    ReservaClienteDTO editarReservaAdmin(ReservaClienteDTO reservaClienteDTO);

    void eliminarReserva(Long id);

    boolean esReservaDelUsuario(Long reservaId);
}
