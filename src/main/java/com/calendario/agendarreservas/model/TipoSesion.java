package com.calendario.agendarreservas.model;

import lombok.Getter;

@Getter
public enum TipoSesion {
    INICIAL(1, "Sesión Inicial"),
    SEGUIMIENTO(2, "Seguimiento"),
    CRISIS(3, "Crisis"),
    EVALUACION(4, "Evaluación"),
    CIERRE(5, "Cierre / Alta");

    private final int id;
    private final String label;

    TipoSesion(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public static TipoSesion findById(int id) {
        for (TipoSesion t : values()) {
            if (t.id == id) return t;
        }
        throw new IllegalArgumentException("TipoSesion id inválido: " + id);
    }
}
