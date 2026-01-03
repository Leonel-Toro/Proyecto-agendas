package com.Calendario.AgendarReservas.Model;

import jakarta.persistence.*;
import org.antlr.v4.runtime.misc.NotNull;

import java.sql.Timestamp;

@Entity
@Table(name = "reserva")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva")
    private Long idReserva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Column(name = "fecha_reserva", nullable = false)
    private Timestamp fechaReserva;

    @Column(name = "lugar_encuentro")
    private String lugarEncuentro;

    @Column(name = "fecha_termino")
    private Timestamp fechaTermino;

    @NotNull
    @Column(name = "precio", nullable = false)
    private Long precio = 0L;

    @Column(name= "abonado")
    private Long abonado = 0L;

    @Column(name = "nombre_producto")
    private String nombreProducto;

    @Column(name = "mensaje_personalizado")
    private String mensajePersonalizado;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoReserva estado;

    @Column(name = "fecha_creacion", nullable = false, insertable = false, updatable = false)
    private Timestamp fechaCreacion;

    @Column(name = "fecha_modificacion", nullable = false, insertable = false, updatable = false)
    private Timestamp fechaModificacion;

    public Reserva() {
    }

    public Reserva(Long idReserva, Cliente cliente, Timestamp fechaReserva, Timestamp fechaModificacion, Timestamp fechaCreacion, EstadoReserva estado, Long precio, Timestamp fechaTermino, String lugarEncuentro) {
        this.idReserva = idReserva;
        this.cliente = cliente;
        this.fechaReserva = fechaReserva;
        this.fechaModificacion = fechaModificacion;
        this.fechaCreacion = fechaCreacion;
        this.estado = estado;
        this.precio = precio;
        this.fechaTermino = fechaTermino;
        this.lugarEncuentro = lugarEncuentro;
    }

    public Long getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(Long idReserva) {
        this.idReserva = idReserva;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Timestamp getFechaReserva() {
        return fechaReserva;
    }

    public void setFechaReserva(Timestamp fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public String getLugarEncuentro() {
        return lugarEncuentro;
    }

    public void setLugarEncuentro(String lugarEncuentro) {
        this.lugarEncuentro = lugarEncuentro;
    }

    public Timestamp getFechaTermino() {
        return fechaTermino;
    }

    public void setFechaTermino(Timestamp fechaTermino) {
        this.fechaTermino = fechaTermino;
    }

    public Long getPrecio() {
        return precio;
    }

    public void setPrecio(Long precio) {
        this.precio = precio;
    }

    public Long getAbonado() {
        return abonado;
    }

    public void setAbonado(Long abonado) {
        this.abonado = abonado;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    public Timestamp getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Timestamp fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Timestamp getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(Timestamp fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}