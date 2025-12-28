package com.Calendario.AgendarReservas.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Long idCliente;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    // Si "medio" luego referenciará otra tabla, de momento lo dejamos como BIGINT simple
    @Column(name = "medio")
    private MedioContacto medio;

    public Cliente() {
    }

    public Cliente(Long idCliente, String nombre, MedioContacto medio) {
        this.idCliente = idCliente;
        this.nombre = nombre;
        this.medio = medio;
    }

    // Getters y setters
    public Long getIdCliente() { return idCliente; }
    public void setIdCliente(Long idCliente) { this.idCliente = idCliente; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public MedioContacto getMedio() { return medio; }
    public void setMedio(MedioContacto medio) { this.medio = medio; }
}
