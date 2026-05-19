package com.calendario.agendarreservas.mapper;

import com.calendario.agendarreservas.dto.NotasSesionDTO;
import com.calendario.agendarreservas.model.NotasSesion;
import org.springframework.stereotype.Component;

@Component
public class NotasMapper {

    public NotasSesionDTO toDTO(NotasSesion n) {
        NotasSesionDTO dto = new NotasSesionDTO();
        dto.setIdNota(n.getIdNota());
        dto.setIdReserva(n.getHistorialPaciente().getReserva().getIdReserva());
        dto.setNota(n.getNota());
        dto.setFechaCreacion(n.getFechaCreacion());
        return dto;
    }
}
