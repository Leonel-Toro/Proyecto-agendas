package com.calendario.agendarreservas.mapper;

import com.calendario.agendarreservas.dto.ReservaDTO;
import com.calendario.agendarreservas.model.Reserva;
import com.calendario.agendarreservas.model.User;
import org.springframework.stereotype.Component;

@Component
public class ReservaMapper {

    public ReservaDTO toDTO(Reserva r) {
        ReservaDTO dto = new ReservaDTO();
        dto.setId(r.getIdReserva());
        dto.setPacienteId(r.getPaciente().getId());
        dto.setPacienteNombre(fullName(r.getPaciente()));
        dto.setPacienteRut(r.getPaciente().getRut());
        dto.setPsicologoId(r.getPsicologo().getId());
        dto.setPsicologoNombre(fullName(r.getPsicologo()));
        dto.setMotivoConsulta(r.getMotivoConsulta());
        dto.setModalidad(r.getModalidad().name());
        dto.setDuracionMinutos(r.getDuracionMinutos());
        dto.setFechaReserva(r.getFechaReserva());
        dto.setFechaTermino(r.getFechaTermino());
        dto.setPrecio(r.getPrecio());
        dto.setAbonado(r.getAbonado());
        dto.setEstado(r.getEstado().name());
        dto.setFechaCreacion(r.getFechaCreacion());
        dto.setFechaModificacion(r.getFechaModificacion());
        return dto;
    }

    private String fullName(User u) {
        if (u.getNombre() == null) return u.getUsername();
        if (u.getApellidos() == null) return u.getNombre();
        return u.getNombre() + " " + u.getApellidos();
    }
}
