package com.calendario.agendarreservas.mapper;

import com.calendario.agendarreservas.dto.HistorialPacienteDTO;
import com.calendario.agendarreservas.dto.NotasSesionDTO;
import com.calendario.agendarreservas.model.HistorialPaciente;
import com.calendario.agendarreservas.model.NotasSesion;
import com.calendario.agendarreservas.model.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HistorialPacienteMapper {

    public HistorialPacienteDTO toDTO(HistorialPaciente h) {
        HistorialPacienteDTO dto = new HistorialPacienteDTO();
        dto.setIdHistorial(h.getIdHistorial());
        dto.setIdReserva(h.getReserva().getIdReserva());
        dto.setPacienteId(h.getPaciente().getId());
        dto.setPacienteNombre(fullName(h.getPaciente()));
        dto.setPacienteRut(h.getPaciente().getRut());
        dto.setPsicologoId(h.getPsicologo().getId());
        dto.setPsicologoNombre(fullName(h.getPsicologo()));
        dto.setMotivoConsulta(h.getMotivoConsulta());
        dto.setTipoSesion(h.getTipoSesion() != null ? h.getTipoSesion().name() : null);
        dto.setCrisis(h.getCrisis());
        dto.setAlta(h.getAlta());
        dto.setPosibleAbandono(h.getPosibleAbandono());
        dto.setNotasGenerales(h.getNotasGenerales());
        dto.setFechaCreacion(h.getFechaCreacion());
        dto.setFechaActualizacion(h.getFechaActualizacion());

        List<NotasSesionDTO> notas = h.getNotasSesion().stream()
                .map(this::notaToDTO)
                .toList();
        dto.setNotasSesion(notas);

        return dto;
    }

    public NotasSesionDTO notaToDTO(NotasSesion n) {
        return new NotasSesionDTO(
                n.getIdNota(),
                n.getHistorialPaciente().getIdHistorial(),
                n.getNota(),
                n.getFechaCreacion()
        );
    }

    private String fullName(User u) {
        if (u.getNombre() == null) return u.getUsername();
        if (u.getApellidos() == null) return u.getNombre();
        return u.getNombre() + " " + u.getApellidos();
    }
}
