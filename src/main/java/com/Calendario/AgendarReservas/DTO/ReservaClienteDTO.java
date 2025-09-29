package com.Calendario.AgendarReservas.DTO;
import java.sql.Timestamp;

public class ReservaClienteDTO {
    private Long precio;
    private String estado;
    private String nombreProducto;
    private Timestamp fechaReserva;
    private Timestamp fechaTermino;
    private String lugarEncuentro;
    private String nombreCliente;
    private String emailCliente;
    private String telefonoCliente;
    private String medioCliente;
    private String mensajePersonalizado;

    public ReservaClienteDTO() {
    }

    public Long getPrecio() {
        return precio;
    }

    public void setPrecio(Long precio) {
        this.precio = precio;
    }

    public Timestamp getFechaReserva() {
        return fechaReserva;
    }

    public void setFechaReserva(Timestamp fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Timestamp getFechaTermino() {
        return fechaTermino;
    }

    public void setFechaTermino(Timestamp fechaTermino) {
        this.fechaTermino = fechaTermino;
    }

    public String getLugarEncuentro() {
        return lugarEncuentro;
    }

    public void setLugarEncuentro(String lugarEncuentro) {
        this.lugarEncuentro = lugarEncuentro;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getEmailCliente() {
        return emailCliente;
    }

    public void setEmailCliente(String emailCliente) {
        this.emailCliente = emailCliente;
    }

    public String getTelefonoCliente() {
        return telefonoCliente;
    }

    public void setTelefonoCliente(String telefonoCliente) {
        this.telefonoCliente = telefonoCliente;
    }

    public String getMedioCliente() {
        return medioCliente;
    }

    public void setMedioCliente(String medioCliente) {
        this.medioCliente = medioCliente;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public String getMensajePersonalizado() {
        return mensajePersonalizado;
    }

    public void setMensajePersonalizado(String mensajePersonalizado) {
        this.mensajePersonalizado = mensajePersonalizado;
    }
}
