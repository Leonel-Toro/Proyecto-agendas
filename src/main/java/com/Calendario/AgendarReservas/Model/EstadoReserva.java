package com.Calendario.AgendarReservas.Model;

public enum EstadoReserva {
    PENDIENTE(1, "Pendiente"),
    ABONADA(2, "Abonada"),
    CANCELADA(3, "Cancelada"),
    NO_CONCRETADA(4, "No concretada"),
    PAGADA(5, "Pagada");

    private final int id;
    private final String label;

    EstadoReserva(int id, String label) {
        this.id = id;
        this.label = label;
    }
    public int getId() { return id; }
    public String getLabel() { return label; }

    public static EstadoReserva findEstado(int idEstado){
        for (EstadoReserva e : values()) {
            if (e.id == idEstado) return e;
        }
        throw new IllegalArgumentException("EstadoReserva id inválido: " + idEstado);
    }
}
