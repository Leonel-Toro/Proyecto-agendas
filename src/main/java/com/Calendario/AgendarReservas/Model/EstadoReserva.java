package com.calendario.agendarreservas.model;

import lombok.Getter;

@Getter
public enum EstadoReserva {
    PENDIENTE(1, "Pendiente"),
    CONFIRMADA(2, "Confirmada"),
    CANCELADA(3, "Cancelada"),
    COMPLETADA(4, "Completada");

    private final int id;
    private final String label;

    EstadoReserva(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public static EstadoReserva findById(int id) {
        for (EstadoReserva e : values()) {
            if (e.id == id) return e;
        }
        throw new IllegalArgumentException("EstadoReserva id inválido: " + id);
    }
}
