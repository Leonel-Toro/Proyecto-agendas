package com.calendario.agendarreservas.service.impl;

import com.calendario.agendarreservas.dto.DisponibilidadDTO;
import com.calendario.agendarreservas.dto.ReservaDTO;
import com.calendario.agendarreservas.exception.ConflictException;
import com.calendario.agendarreservas.exception.ResourceNotFoundException;
import com.calendario.agendarreservas.exception.UnauthorizedOperationException;
import com.calendario.agendarreservas.event.ReservaEstadoEvent;
import com.calendario.agendarreservas.mapper.ReservaMapper;
import com.calendario.agendarreservas.model.*;
import com.calendario.agendarreservas.repository.ReservaRepository;
import com.calendario.agendarreservas.repository.UserRepository;
import com.calendario.agendarreservas.service.ReservaService;
import com.calendario.agendarreservas.util.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservaServiceImpl implements ReservaService {

    private static final Logger logger = LoggerFactory.getLogger(ReservaServiceImpl.class);

    private final ReservaRepository reservaRepository;
    private final UserRepository userRepository;
    private final SecurityContextHelper securityContextHelper;
    private final ReservaMapper reservaMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ReservaDTO crearReserva(ReservaDTO dto) {
        if (dto.getFechaReserva() == null)
            throw new IllegalArgumentException("La fecha de reserva es obligatoria.");
        if (dto.getFechaReserva().toLocalDateTime().isBefore(LocalDateTime.now()))
            throw new IllegalArgumentException("La fecha de reserva debe ser futura.");
        if (dto.getPsicologoId() == null)
            throw new IllegalArgumentException("Debe seleccionar un psicólogo.");
        if (dto.getPrecio() == null || dto.getPrecio() < 0)
            throw new IllegalArgumentException("El precio debe ser mayor o igual a 0.");
        validarDuracion(dto.getDuracionMinutos());
        validarModalidad(dto.getModalidad());

        User paciente = securityContextHelper.getCurrentUser();
        User psicologo = userRepository.findById(dto.getPsicologoId())
                .orElseThrow(() -> new ResourceNotFoundException("Psicólogo", dto.getPsicologoId()));

        Reserva r = buildReserva(dto, paciente, psicologo);
        validarHorarioLaboral(r.getFechaReserva(), r.getFechaTermino());
        validarDisponibilidad(psicologo, paciente, r.getFechaReserva(), r.getFechaTermino(), null);
        r.setEstado(EstadoReserva.PENDIENTE);
        reservaRepository.save(r);
        publicarCambioEstado(r, true);
        return reservaMapper.toDTO(r);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaDTO> obtenerMisReservas() {
        Long userId = securityContextHelper.getCurrentUserId();
        return reservaRepository.findByPacienteIdOrderByFechaReservaDesc(userId)
                .stream().map(reservaMapper::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReservaDTO obtenerMiReserva(Long id) {
        Long userId = securityContextHelper.getCurrentUserId();
        return reservaMapper.toDTO(
                reservaRepository.findByIdReservaAndPacienteId(id, userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Reserva", id)));
    }

    @Override
    @Transactional
    public ReservaDTO editarReserva(Long id, ReservaDTO dto) {
        Long userId = securityContextHelper.getCurrentUserId();
        Reserva r = reservaRepository.findByIdReservaAndPacienteId(id, userId)
                .orElseThrow(() -> new UnauthorizedOperationException(
                        "Reserva no encontrada o sin permisos para editarla."));

        validarEstadoEditable(r);
        validarAntelacion24h(r.getFechaReserva());
        applyPatientUpdates(r, dto);
        validarHorarioLaboral(r.getFechaReserva(), r.getFechaTermino());
        validarDisponibilidad(r.getPsicologo(), r.getPaciente(), r.getFechaReserva(), r.getFechaTermino(), r.getIdReserva());
        reservaRepository.save(r);
        return reservaMapper.toDTO(r);
    }

    @Override
    @Transactional
    public void cancelarReserva(Long id) {
        Long userId = securityContextHelper.getCurrentUserId();
        Reserva r = reservaRepository.findByIdReservaAndPacienteId(id, userId)
                .orElseThrow(() -> new UnauthorizedOperationException(
                        "Reserva no encontrada o sin permisos para cancelarla."));

        validarEstadoCancelable(r);
        validarAntelacion24h(r.getFechaReserva());
        r.setEstado(EstadoReserva.CANCELADA);
        reservaRepository.save(r);
        publicarCambioEstado(r, false);
    }

    @Override
    @Transactional
    public ReservaDTO crearReservaAdmin(ReservaDTO dto) {
        if (dto.getPacienteId() == null)
            throw new IllegalArgumentException("El ID del paciente es obligatorio.");
        if (dto.getPsicologoId() == null)
            throw new IllegalArgumentException("El ID del psicólogo es obligatorio.");
        validarDuracion(dto.getDuracionMinutos());
        validarModalidad(dto.getModalidad());

        User paciente = userRepository.findById(dto.getPacienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", dto.getPacienteId()));
        User psicologo = userRepository.findById(dto.getPsicologoId())
                .orElseThrow(() -> new ResourceNotFoundException("Psicólogo", dto.getPsicologoId()));

        Reserva r = buildReserva(dto, paciente, psicologo);
        validarHorarioLaboral(r.getFechaReserva(), r.getFechaTermino());
        validarDisponibilidad(psicologo, paciente, r.getFechaReserva(), r.getFechaTermino(), null);
        r.setEstado(dto.getEstado() != null && !dto.getEstado().isBlank()
                ? EstadoReserva.valueOf(dto.getEstado().toUpperCase())
                : EstadoReserva.PENDIENTE);
        reservaRepository.save(r);
        publicarCambioEstado(r, true);
        return reservaMapper.toDTO(r);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaDTO> obtenerTodasReservas() {
        return reservaRepository.findAll().stream().map(reservaMapper::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaDTO> obtenerReservasPorPaciente(Long pacienteId) {
        userRepository.findById(pacienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", pacienteId));
        return reservaRepository.findByPacienteIdOrderByFechaReservaDesc(pacienteId)
                .stream().map(reservaMapper::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReservaDTO obtenerReservaAdmin(Long id) {
        return reservaMapper.toDTO(
                reservaRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Reserva", id)));
    }

    @Override
    @Transactional
    public ReservaDTO editarReservaAdmin(Long id, ReservaDTO dto) {
        Reserva r = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", id));
        EstadoReserva estadoAnterior = r.getEstado();
        applyAdminUpdates(r, dto);
        validarHorarioLaboral(r.getFechaReserva(), r.getFechaTermino());
        validarDisponibilidad(r.getPsicologo(), r.getPaciente(), r.getFechaReserva(), r.getFechaTermino(), r.getIdReserva());
        reservaRepository.save(r);
        if (r.getEstado() != estadoAnterior) {
            logger.info("Cambio de estado en reserva id={}: {} -> {}", r.getIdReserva(), estadoAnterior, r.getEstado());
            publicarCambioEstado(r, false);
        }
        return reservaMapper.toDTO(r);
    }

    @Override
    @Transactional
    public void cancelarReservaAdmin(Long id) {
        Reserva r = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", id));
        validarEstadoCancelable(r);
        r.setEstado(EstadoReserva.CANCELADA);
        reservaRepository.save(r);
        publicarCambioEstado(r, false);
    }

    @Override
    @Transactional
    public ReservaDTO completarReserva(Long id) {
        Reserva r = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", id));
        if (r.getEstado() == EstadoReserva.CANCELADA)
            throw new IllegalArgumentException("No se puede completar una reserva cancelada.");
        r.setEstado(EstadoReserva.COMPLETADA);
        reservaRepository.save(r);
        publicarCambioEstado(r, false);
        return reservaMapper.toDTO(r);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DisponibilidadDTO> obtenerHorariosOcupados(Long psicologoId, LocalDate fecha, Long excludeReservaId) {
        userRepository.findById(psicologoId)
                .orElseThrow(() -> new ResourceNotFoundException("Psicólogo", psicologoId));
        Timestamp desde = Timestamp.valueOf(fecha.atStartOfDay());
        Timestamp hasta = Timestamp.valueOf(fecha.atTime(LocalTime.MAX));
        return reservaRepository.findOcupadosPsicologo(psicologoId, desde, hasta, excludeReservaId).stream()
                .map(r -> new DisponibilidadDTO(r.getFechaReserva(), r.getFechaTermino()))
                .toList();
    }

    // ---- helpers ----

    /** Publica el evento de cambio de estado y deja traza (sin datos sensibles). */
    private void publicarCambioEstado(Reserva r, boolean esCreacion) {
        logger.info("Publicando notificacion reserva id={} estado={} creacion={}",
                r.getIdReserva(), r.getEstado(), esCreacion);
        eventPublisher.publishEvent(new ReservaEstadoEvent(r.getIdReserva(), r.getEstado(), esCreacion));
    }

    private static final LocalTime HORA_APERTURA = LocalTime.of(8, 0);
    private static final LocalTime HORA_CIERRE = LocalTime.of(23, 59, 59);

    private Timestamp calcularFechaTermino(Timestamp fechaReserva, Integer duracionMinutos) {
        return Timestamp.valueOf(fechaReserva.toLocalDateTime().plusMinutes(duracionMinutos));
    }

    private void validarHorarioLaboral(Timestamp inicio, Timestamp fin) {
        LocalDateTime ini = inicio.toLocalDateTime();
        LocalDateTime fim = fin.toLocalDateTime();
        if (!ini.toLocalDate().equals(fim.toLocalDate()))
            throw new IllegalArgumentException("La reserva no puede extenderse más allá de la medianoche.");
        if (ini.toLocalTime().isBefore(HORA_APERTURA) || fim.toLocalTime().isAfter(HORA_CIERRE))
            throw new IllegalArgumentException("El horario debe estar entre las 08:00 y las 23:59.");
    }

    private void validarDisponibilidad(User psicologo, User paciente, Timestamp inicio, Timestamp fin, Long excludeReservaId) {
        if (!reservaRepository.findSolapamientosPsicologo(psicologo.getId(), inicio, fin, excludeReservaId).isEmpty())
            throw new ConflictException("El psicólogo ya tiene una reserva en ese horario.");
        if (!reservaRepository.findSolapamientosPaciente(paciente.getId(), inicio, fin, excludeReservaId).isEmpty())
            throw new ConflictException("Ya tienes una reserva en ese horario.");
    }

    private Reserva buildReserva(ReservaDTO dto, User paciente, User psicologo) {
        Reserva r = new Reserva();
        r.setPaciente(paciente);
        r.setPsicologo(psicologo);
        r.setMotivoConsulta(dto.getMotivoConsulta());
        r.setModalidad(Modalidad.valueOf(dto.getModalidad().toUpperCase()));
        r.setDuracionMinutos(dto.getDuracionMinutos() != null ? dto.getDuracionMinutos() : 60);
        r.setFechaReserva(dto.getFechaReserva());
        r.setFechaTermino(calcularFechaTermino(r.getFechaReserva(), r.getDuracionMinutos()));
        r.setPrecio(dto.getPrecio() != null ? dto.getPrecio() : 0L);
        r.setAbonado(dto.getAbonado() != null ? dto.getAbonado() : 0L);
        return r;
    }

    private void applyPatientUpdates(Reserva r, ReservaDTO dto) {
        if (dto.getMotivoConsulta() != null) r.setMotivoConsulta(dto.getMotivoConsulta());
        if (dto.getModalidad() != null) {
            validarModalidad(dto.getModalidad());
            r.setModalidad(Modalidad.valueOf(dto.getModalidad().toUpperCase()));
        }
        if (dto.getDuracionMinutos() != null) {
            validarDuracion(dto.getDuracionMinutos());
            r.setDuracionMinutos(dto.getDuracionMinutos());
        }
        if (dto.getFechaReserva() != null) {
            if (dto.getFechaReserva().toLocalDateTime().isBefore(LocalDateTime.now()))
                throw new IllegalArgumentException("La nueva fecha debe ser futura.");
            r.setFechaReserva(dto.getFechaReserva());
        }
        r.setFechaTermino(calcularFechaTermino(r.getFechaReserva(), r.getDuracionMinutos()));
    }

    private void applyAdminUpdates(Reserva r, ReservaDTO dto) {
        applyPatientUpdates(r, dto);
        if (dto.getPrecio() != null && dto.getPrecio() >= 0) r.setPrecio(dto.getPrecio());
        if (dto.getAbonado() != null && dto.getAbonado() >= 0) {
            if (dto.getAbonado() > r.getPrecio())
                throw new IllegalArgumentException("El monto abonado no puede superar el precio.");
            r.setAbonado(dto.getAbonado());
        }
        if (dto.getEstado() != null && !dto.getEstado().isBlank())
            r.setEstado(EstadoReserva.valueOf(dto.getEstado().toUpperCase()));
    }

    private void validarAntelacion24h(Timestamp fechaReserva) {
        if (fechaReserva.toLocalDateTime().isBefore(LocalDateTime.now().plusHours(24)))
            throw new IllegalArgumentException(
                    "Solo se puede modificar o cancelar con al menos 24 horas de anticipación.");
    }

    private void validarDuracion(Integer minutos) {
        int d = minutos != null ? minutos : 60;
        if (d < 30 || d > 360 || d % 30 != 0)
            throw new IllegalArgumentException(
                    "La duración debe ser múltiplo de 30 entre 30 y 360 minutos.");
    }

    private void validarModalidad(String modalidad) {
        if (modalidad == null || modalidad.isBlank())
            throw new IllegalArgumentException("La modalidad es obligatoria (PRESENCIAL o VIRTUAL).");
        try {
            Modalidad.valueOf(modalidad.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Modalidad inválida. Use PRESENCIAL o VIRTUAL.");
        }
    }

    private void validarEstadoEditable(Reserva r) {
        if (r.getEstado() != EstadoReserva.PENDIENTE && r.getEstado() != EstadoReserva.CONFIRMADA)
            throw new IllegalArgumentException(
                    "No se puede editar una reserva con estado " + r.getEstado().getLabel() + ".");
    }

    private void validarEstadoCancelable(Reserva r) {
        if (r.getEstado() == EstadoReserva.CANCELADA || r.getEstado() == EstadoReserva.COMPLETADA)
            throw new IllegalArgumentException(
                    "La reserva ya está " + r.getEstado().getLabel() + ".");
    }
}
