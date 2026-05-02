package com.calendario.agendarreservas.mapper;

import com.calendario.agendarreservas.dto.ReservaClienteDTO;
import com.calendario.agendarreservas.model.Cliente;
import com.calendario.agendarreservas.model.Reserva;
import org.springframework.stereotype.Component;

@Component
public class ReservaMapper {

    public ReservaClienteDTO toDTO(Reserva reserva) {
        ReservaClienteDTO dto = new ReservaClienteDTO();
        dto.setId(reserva.getIdReserva());
        dto.setNombreProducto(reserva.getNombreProducto());
        dto.setMensajePersonalizado(reserva.getMensajePersonalizado());
        dto.setFechaTermino(reserva.getFechaTermino());
        dto.setPrecio(reserva.getPrecio());
        dto.setEstado(reserva.getEstado().name());
        dto.setFechaReserva(reserva.getFechaReserva());
        dto.setLugarEncuentro(reserva.getLugarEncuentro());
        dto.setAbonado(reserva.getAbonado());

        Cliente cliente = reserva.getCliente();
        if (cliente != null) {
            dto.setNombreCliente(cliente.getNombre());
            dto.setMedioCliente(cliente.getMedio() != null ? cliente.getMedio().name() : null);
        }
        return dto;
    }
}
