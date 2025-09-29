package com.Calendario.AgendarReservas.Model;

public enum MedioContacto {
    DIRECTO(1, "Directo"),
    FACEBOOK(2, "Facebook"),
    INSTAGRAM(3, "Instagram"),
    OTRO(99, "Otro");

    private final int id;
    private final String label;

    MedioContacto(int id, String label) {
        this.id = id;
        this.label = label;
    }
    public int getId() { return id; }
    public String getLabel() { return label; }

    public static MedioContacto findById(int idMedio){
        for (MedioContacto m : values()) {
            if (m.id == idMedio) return m;
        }
        throw new IllegalArgumentException("MedioContacto id inválido: " + idMedio);
    }
}