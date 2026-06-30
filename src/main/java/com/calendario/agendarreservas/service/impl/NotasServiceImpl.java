package com.calendario.agendarreservas.service.impl;

import com.calendario.agendarreservas.dto.NotasSesionDTO;
import com.calendario.agendarreservas.exception.ResourceNotFoundException;
import com.calendario.agendarreservas.exception.UnauthorizedOperationException;
import com.calendario.agendarreservas.mapper.NotasMapper;
import com.calendario.agendarreservas.model.HistorialPaciente;
import com.calendario.agendarreservas.model.NotasSesion;
import com.calendario.agendarreservas.repository.HistorialPacienteRepository;
import com.calendario.agendarreservas.repository.NotasSesionRepository;
import com.calendario.agendarreservas.service.NotasService;
import com.calendario.agendarreservas.util.SecurityContextHelper;
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
    private final SecurityContextHelper securityContextHelper;

    @Override
    @Transactional
    public NotasSesionDTO crearNota(NotasSesionDTO dto) {
        if (dto.getIdReserva() == null)
            throw new IllegalArgumentException("El id_reserva es obligatorio.");

        HistorialPaciente historial = historialRepository
                .findByReservaIdReserva(dto.getIdReserva())
                .orElseThrow(() -> new ResourceNotFoundException("HistorialPaciente para reserva", dto.getIdReserva()));
        Long me = securityContextHelper.getCurrentUserId();
        if (!historial.getPsicologo().getId().equals(me))
            throw new UnauthorizedOperationException("Sin permisos para agregar notas a esta reserva.");

        NotasSesion nota = new NotasSesion();
        nota.setHistorialPaciente(historial);
        nota.setNota(dto.getNota());

        return notasMapper.toDTO(notasRepository.save(nota));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotasSesionDTO> listarNotasPorReserva(Long idReserva) {
        HistorialPaciente historial = historialRepository.findByReservaIdReserva(idReserva)
                .orElseThrow(() -> new ResourceNotFoundException("HistorialPaciente para reserva", idReserva));
        Long me = securityContextHelper.getCurrentUserId();
        if (!historial.getPsicologo().getId().equals(me))
            throw new UnauthorizedOperationException("Sin permisos para ver las notas de esta reserva.");

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
        Long me = securityContextHelper.getCurrentUserId();
        if (!nota.getHistorialPaciente().getPsicologo().getId().equals(me))
            throw new UnauthorizedOperationException("Sin permisos para modificar esta nota.");

        nota.setNota(dto.getNota());
        return notasMapper.toDTO(notasRepository.save(nota));
    }

    @Override
    @Transactional
    public void eliminarNota(Long idNota) {
        NotasSesion nota = notasRepository.findById(idNota)
                .orElseThrow(() -> new ResourceNotFoundException("Nota", idNota));
        Long me = securityContextHelper.getCurrentUserId();
        if (!nota.getHistorialPaciente().getPsicologo().getId().equals(me))
            throw new UnauthorizedOperationException("Sin permisos para eliminar esta nota.");

        notasRepository.delete(nota);
    }
}
