package com.calendario.agendarreservas.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservaClienteDTO {
    private long id;
    private Long precio;
    private String estado;
    private String nombreProducto;
    private Timestamp fechaReserva;
    private Timestamp fechaTermino;
    private String lugarEncuentro;
    private String nombreCliente;
    private String medioCliente;
    private String mensajePersonalizado;
    private Long abonado;
}
