package com.calendario.agendarreservas.model;

import lombok.Getter;

@Getter
public enum Modalidad {
    PRESENCIAL(1, "Presencial"),
    VIRTUAL(2, "Virtual");

    private final int id;
    private final String label;

    Modalidad(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public static Modalidad findById(int id) {
        for (Modalidad m : values()) {
            if (m.id == id) return m;
        }
        throw new IllegalArgumentException("Modalidad id inválido: " + id);
    }
}
