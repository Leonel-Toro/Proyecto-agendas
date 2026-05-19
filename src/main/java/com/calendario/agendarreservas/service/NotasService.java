package com.calendario.agendarreservas.service;

import com.calendario.agendarreservas.dto.NotasSesionDTO;

import java.util.List;

public interface NotasService {

    NotasSesionDTO crearNota(NotasSesionDTO dto);

    List<NotasSesionDTO> listarNotasPorReserva(Long idReserva);

    NotasSesionDTO modificarNota(Long idNota, NotasSesionDTO dto);

    void eliminarNota(Long idNota);
}
