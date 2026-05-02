package com.calendario.agendarreservas.service.impl;

import com.calendario.agendarreservas.dto.ReservaClienteDTO;
import com.calendario.agendarreservas.exception.ResourceNotFoundException;
import com.calendario.agendarreservas.exception.UnauthorizedOperationException;
import com.calendario.agendarreservas.mapper.ReservaMapper;
import com.calendario.agendarreservas.model.*;
import com.calendario.agendarreservas.repository.ClienteRepository;
import com.calendario.agendarreservas.repository.ReservaRepository;
import com.calendario.agendarreservas.repository.UserRepository;
import com.calendario.agendarreservas.service.ReservaService;
import com.calendario.agendarreservas.util.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository reservaRepository;
    private final ClienteRepository clienteRepository;
    private final UserRepository userRepository;
    private final SecurityContextHelper securityContextHelper;
    private final ReservaMapper reservaMapper;

    @Override
    @Transactional
    public ReservaClienteDTO agendarCliente(ReservaClienteDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Payload vacío.");

        User currentUser = securityContextHelper.getCurrentUser();

        if (dto.getPrecio() == null || dto.getPrecio() < 0)
            throw new IllegalArgumentException("El precio debe ser mayor 0.");

        if (dto.getAbonado() == null || dto.getAbonado() < 0)
            throw new IllegalArgumentException("El monto abonado debe ser mayor a 0.");

        if (dto.getEstado() == null || dto.getEstado().isBlank())
            throw new IllegalArgumentException("El estado es obligatorio.");

        if (dto.getFechaReserva() == null)
            throw new IllegalArgumentException("La fecha de reserva es obligatoria.");

        if (dto.getFechaReserva().toLocalDateTime().isBefore(LocalDateTime.now()))
            throw new IllegalArgumentException("La fecha de inicio debe ser futura.");

        if (dto.getNombreCliente() == null || dto.getNombreCliente().isBlank())
            throw new IllegalArgumentException("El nombre del cliente es obligatorio.");

        if (dto.getMedioCliente() == null || dto.getMedioCliente().isBlank())
            throw new IllegalArgumentException("Debe seleccionar el medio por el cual fue contactado.");

        if (dto.getNombreProducto() == null || dto.getNombreProducto().isBlank())
            throw new IllegalArgumentException("El nombre del producto no debe ser vacío.");

        Cliente cliente = new Cliente();
        cliente.setNombre(dto.getNombreCliente().trim());
        cliente.setMedio(MedioContacto.findById(Integer.parseInt(dto.getMedioCliente())));
        clienteRepository.save(cliente);

        Reserva r = new Reserva();
        r.setFechaReserva(dto.getFechaReserva());
        r.setPrecio(dto.getPrecio());
        r.setEstado(EstadoReserva.findEstado(Integer.parseInt(dto.getEstado())));
        r.setLugarEncuentro(dto.getLugarEncuentro());
        r.setNombreProducto(dto.getNombreProducto());
        r.setMensajePersonalizado(dto.getMensajePersonalizado());
        r.setAbonado(dto.getAbonado());
        r.setUser(currentUser);
        r.setCliente(cliente);
        reservaRepository.save(r);

        dto.setId(r.getIdReserva());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaClienteDTO> obtenerHistorial() {
        Long userId = securityContextHelper.getCurrentUserId();
        return reservaRepository.findByUserIdOrderByFechaReservaDesc(userId)
                .stream().map(reservaMapper::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaClienteDTO> obtenerHistorialPorUsuario(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", userId));
        return reservaRepository.findByUserIdOrderByFechaReservaDesc(userId)
                .stream().map(reservaMapper::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaClienteDTO> obtenerHistorialPorEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con email " + email + " no encontrado"));
        return reservaRepository.findByUserIdOrderByFechaReservaDesc(user.getId())
                .stream().map(reservaMapper::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReservaClienteDTO obtenerDetalleReserva(Long id) {
        Long userId = securityContextHelper.getCurrentUserId();
        Reserva reserva = reservaRepository.findByIdReservaAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", id));
        return reservaMapper.toDTO(reserva);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservaClienteDTO obtenerDetalleReservaAdmin(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", id));
        return reservaMapper.toDTO(reserva);
    }

    @Override
    @Transactional
    public ReservaClienteDTO editarReserva(ReservaClienteDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Payload vacío.");

        Long userId = securityContextHelper.getCurrentUserId();
        Reserva r = reservaRepository.findByIdReservaAndUserId(dto.getId(), userId)
                .orElseThrow(() -> new UnauthorizedOperationException(
                        "No se encontró la reserva o no tiene permisos para editarla."));

        applyUpdates(r, dto);
        reservaRepository.save(r);
        return reservaMapper.toDTO(r);
    }

    @Override
    @Transactional
    public ReservaClienteDTO editarReservaAdmin(ReservaClienteDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Payload vacío.");

        Reserva r = reservaRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", dto.getId()));

        applyUpdates(r, dto);
        reservaRepository.save(r);
        return reservaMapper.toDTO(r);
    }

    @Override
    @Transactional
    public void eliminarReserva(Long id) {
        Long userId = securityContextHelper.getCurrentUserId();
        Reserva reserva = reservaRepository.findByIdReservaAndUserId(id, userId)
                .orElseThrow(() -> new UnauthorizedOperationException(
                        "No se encontró la reserva o no tiene permisos para eliminarla."));
        reservaRepository.delete(reserva);
    }

    @Override
    public boolean esReservaDelUsuario(Long reservaId) {
        Long userId = securityContextHelper.getCurrentUserId();
        return reservaRepository.findByIdReservaAndUserId(reservaId, userId).isPresent();
    }

    private void applyUpdates(Reserva r, ReservaClienteDTO dto) {
        if (dto.getAbonado() != null && dto.getAbonado() < 0)
            throw new IllegalArgumentException("El monto abonado debe ser mayor a 0.");

        if (dto.getAbonado() != null && dto.getPrecio() != null && dto.getAbonado() > dto.getPrecio())
            throw new IllegalArgumentException("El monto abonado no puede ser mayor al precio total.");

        if (dto.getPrecio() != null && dto.getPrecio() >= 0) r.setPrecio(dto.getPrecio());
        if (dto.getAbonado() != null && dto.getAbonado() >= 0) r.setAbonado(dto.getAbonado());

        if (dto.getEstado() != null && !dto.getEstado().isBlank())
            r.setEstado(EstadoReserva.findEstado(Integer.parseInt(dto.getEstado())));

        if (dto.getFechaTermino() != null) {
            if (dto.getFechaTermino().toLocalDateTime().isBefore(r.getFechaReserva().toLocalDateTime()))
                throw new IllegalArgumentException("La fecha de término debe ser mayor a la fecha de reserva.");
            r.setFechaTermino(dto.getFechaTermino());
        }

        if (dto.getLugarEncuentro() != null) r.setLugarEncuentro(dto.getLugarEncuentro());
        if (dto.getNombreProducto() != null && !dto.getNombreProducto().isBlank())
            r.setNombreProducto(dto.getNombreProducto());
        if (dto.getMensajePersonalizado() != null) r.setMensajePersonalizado(dto.getMensajePersonalizado());
    }
}
