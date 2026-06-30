package com.calendario.agendarreservas.service.impl;

import com.calendario.agendarreservas.dto.HistorialPacienteDTO;
import com.calendario.agendarreservas.dto.NotasSesionDTO;
import com.calendario.agendarreservas.exception.ResourceNotFoundException;
import com.calendario.agendarreservas.exception.UnauthorizedOperationException;
import com.calendario.agendarreservas.mapper.HistorialPacienteMapper;
import com.calendario.agendarreservas.model.*;
import com.calendario.agendarreservas.repository.HistorialPacienteRepository;
import com.calendario.agendarreservas.repository.NotasSesionRepository;
import com.calendario.agendarreservas.repository.ReservaRepository;
import com.calendario.agendarreservas.service.HistorialPacienteService;
import com.calendario.agendarreservas.util.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HistorialPacienteServiceImpl implements HistorialPacienteService {

    private final HistorialPacienteRepository historialPacienteRepository;
    private final NotasSesionRepository notasSesionRepository;
    private final ReservaRepository reservaRepository;
    private final SecurityContextHelper securityContextHelper;
    private final HistorialPacienteMapper historialPacienteMapper;

    @Override
    @Transactional
    public HistorialPacienteDTO crearHistorial(HistorialPacienteDTO dto) {
        if (dto.getIdReserva() == null)
            throw new IllegalArgumentException("El ID de la reserva es obligatorio.");
        if (historialPacienteRepository.existsByReservaIdReserva(dto.getIdReserva()))
            throw new IllegalArgumentException("Ya existe un historial para esta reserva.");

        Reserva reserva = reservaRepository.findById(dto.getIdReserva())
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", dto.getIdReserva()));
        Long me = securityContextHelper.getCurrentUserId();
        if (!reserva.getPsicologo().getId().equals(me))
            throw new UnauthorizedOperationException("Sin permisos para crear historial en esta reserva.");

        HistorialPaciente h = new HistorialPaciente();
        h.setReserva(reserva);
        h.setPaciente(reserva.getPaciente());
        h.setPsicologo(reserva.getPsicologo());
        h.setMotivoConsulta(dto.getMotivoConsulta() != null ? dto.getMotivoConsulta() : reserva.getMotivoConsulta());
        h.setTipoSesion(dto.getTipoSesion() != null ? TipoSesion.valueOf(dto.getTipoSesion()) : null);
        h.setCrisis(dto.getCrisis() != null ? dto.getCrisis() : Boolean.FALSE);
        h.setAlta(dto.getAlta() != null ? dto.getAlta() : Boolean.FALSE);
        h.setPosibleAbandono(dto.getPosibleAbandono() != null ? dto.getPosibleAbandono() : Boolean.FALSE);

        historialPacienteRepository.save(h);
        return historialPacienteMapper.toDTO(h);
    }

    @Override
    @Transactional
    public HistorialPacienteDTO actualizarHistorial(Long id, HistorialPacienteDTO dto) {
        HistorialPaciente h = historialPacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Historial", id));
        Long me = securityContextHelper.getCurrentUserId();
        if (!h.getPsicologo().getId().equals(me))
            throw new UnauthorizedOperationException("Sin permisos para actualizar este historial.");

        if (dto.getMotivoConsulta() != null) h.setMotivoConsulta(dto.getMotivoConsulta());
        if (dto.getTipoSesion() != null) h.setTipoSesion(TipoSesion.valueOf(dto.getTipoSesion()));
        if (dto.getCrisis() != null) h.setCrisis(dto.getCrisis());
        if (dto.getAlta() != null) h.setAlta(dto.getAlta());
        if (dto.getPosibleAbandono() != null) h.setPosibleAbandono(dto.getPosibleAbandono());
        if (dto.getNotasGenerales() != null) h.setNotasGenerales(dto.getNotasGenerales());

        historialPacienteRepository.save(h);
        return historialPacienteMapper.toDTO(h);
    }

    @Override
    @Transactional(readOnly = true)
    public HistorialPacienteDTO obtenerHistorialAdmin(Long id) {
        HistorialPaciente h = historialPacienteRepository.findByIdHistorialAndReservaEstado(id, EstadoReserva.COMPLETADA)
                .orElseThrow(() -> new ResourceNotFoundException("Historial", id));
        Long me = securityContextHelper.getCurrentUserId();
        if (!h.getPsicologo().getId().equals(me))
            throw new UnauthorizedOperationException("Sin permisos para acceder a este historial.");
        return historialPacienteMapper.toDTO(h);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialPacienteDTO> obtenerHistorialPorPaciente(Long pacienteId) {
        Long me = securityContextHelper.getCurrentUserId();
        return historialPacienteRepository.findByPacienteIdAndPsicologoIdOrderByFechaCreacionDesc(pacienteId, me)
                .stream().map(historialPacienteMapper::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialPacienteDTO> obtenerHistorialPorPacienteYRango(Long pacienteId, Instant desde, Instant hasta) {
        Long me = securityContextHelper.getCurrentUserId();
        return historialPacienteRepository.findByPacienteIdAndPsicologoIdAndRango(pacienteId, me, desde, hasta)
                .stream().map(historialPacienteMapper::toDTO).toList();
    }

    @Override
    @Transactional
    public NotasSesionDTO agregarNota(Long idHistorial, NotasSesionDTO dto) {
        HistorialPaciente h = historialPacienteRepository.findById(idHistorial)
                .orElseThrow(() -> new ResourceNotFoundException("Historial", idHistorial));
        Long me = securityContextHelper.getCurrentUserId();
        if (!h.getPsicologo().getId().equals(me))
            throw new UnauthorizedOperationException("Sin permisos para agregar notas a este historial.");

        NotasSesion nota = new NotasSesion();
        nota.setHistorialPaciente(h);
        nota.setNota(dto.getNota());
        notasSesionRepository.save(nota);

        return new NotasSesionDTO(nota.getIdNota(), idHistorial, nota.getNota(), nota.getFechaCreacion());
    }

    @Override
    @Transactional
    public void eliminarNota(Long idNota) {
        NotasSesion nota = notasSesionRepository.findById(idNota)
                .orElseThrow(() -> new ResourceNotFoundException("Nota", idNota));
        Long me = securityContextHelper.getCurrentUserId();
        if (!nota.getHistorialPaciente().getPsicologo().getId().equals(me))
            throw new UnauthorizedOperationException("Sin permisos para eliminar esta nota.");
        notasSesionRepository.delete(nota);
    }

    @Override
    @Transactional(readOnly = true)
    public HistorialPacienteDTO obtenerMiHistorial(Long id) {
        Long userId = securityContextHelper.getCurrentUserId();
        HistorialPaciente h = historialPacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Historial", id));
        if (!h.getPaciente().getId().equals(userId))
            throw new UnauthorizedOperationException("No tiene permisos para acceder a este historial.");
        return historialPacienteMapper.toDTO(h);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialPacienteDTO> obtenerMiHistorialCompleto() {
        Long userId = securityContextHelper.getCurrentUserId();
        return historialPacienteRepository.findByPacienteIdOrderByFechaCreacionDesc(userId)
                .stream().map(historialPacienteMapper::toDTO).toList();
    }
}
