package com.calendario.agendarreservas.model;

import lombok.Getter;

@Getter
public enum Genero {
    MASCULINO("Él"),
    FEMENINO("Ella"),
    PREFIERO_NO_DECIRLO("Prefiero no decirlo");

    private final String label;

    Genero(String label) {
        this.label = label;
    }
}
