package com.calendario.agendarreservas.service.impl;

import com.calendario.agendarreservas.dto.NotasSesionDTO;
import com.calendario.agendarreservas.exception.ResourceNotFoundException;
import com.calendario.agendarreservas.mapper.NotasMapper;
import com.calendario.agendarreservas.model.HistorialPaciente;
import com.calendario.agendarreservas.model.NotasSesion;
import com.calendario.agendarreservas.repository.HistorialPacienteRepository;
import com.calendario.agendarreservas.repository.NotasSesionRepository;
import com.calendario.agendarreservas.service.NotasService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotasServiceImpl implements NotasService {

    private final NotasSesionRepository notasRepository;
    private final HistorialPacienteRepository historialRepository;
    private final NotasMapper notasMapper;

    @Override
    @Transactional
    public NotasSesionDTO crearNota(NotasSesionDTO dto) {
        if (dto.getIdReserva() == null)
            throw new IllegalArgumentException("El id_reserva es obligatorio.");

        HistorialPaciente historial = historialRepository
                .findByReservaIdReserva(dto.getIdReserva())
                .orElseThrow(() -> new ResourceNotFoundException("HistorialPaciente para reserva", dto.getIdReserva()));

        NotasSesion nota = new NotasSesion();
        nota.setHistorialPaciente(historial);
        nota.setNota(dto.getNota());

        return notasMapper.toDTO(notasRepository.save(nota));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotasSesionDTO> listarNotasPorReserva(Long idReserva) {
        if (!historialRepository.existsByReservaIdReserva(idReserva))
            throw new ResourceNotFoundException("HistorialPaciente para reserva", idReserva);

        return notasRepository
                .findByHistorialPacienteReservaIdReservaOrderByFechaCreacionAsc(idReserva)
                .stream()
                .map(notasMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public NotasSesionDTO modificarNota(Long idNota, NotasSesionDTO dto) {
        NotasSesion nota = notasRepository.findById(idNota)
                .orElseThrow(() -> new ResourceNotFoundException("Nota", idNota));

        nota.setNota(dto.getNota());
        return notasMapper.toDTO(notasRepository.save(nota));
    }

    @Override
    @Transactional
    public void eliminarNota(Long idNota) {
        if (!notasRepository.existsById(idNota))
            throw new ResourceNotFoundException("Nota", idNota);

        notasRepository.deleteById(idNota);
    }
}
