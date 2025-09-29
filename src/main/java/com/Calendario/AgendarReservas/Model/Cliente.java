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

    @Column(name = "email", unique = true, length = 100)
    private String email;

    @Column(name = "telefono", length = 15)
    private String telefono;

    // Si "medio" luego referenciará otra tabla, de momento lo dejamos como BIGINT simple
    @Column(name = "medio")
    private MedioContacto medio;

    public Cliente() {
    }

    public Cliente(Long idCliente, String nombre, String email, String telefono, MedioContacto medio) {
        this.idCliente = idCliente;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.medio = medio;
    }

    // Getters y setters
    public Long getIdCliente() { return idCliente; }
    public void setIdCliente(Long idCliente) { this.idCliente = idCliente; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public MedioContacto getMedio() { return medio; }
    public void setMedio(MedioContacto medio) { this.medio = medio; }
}
