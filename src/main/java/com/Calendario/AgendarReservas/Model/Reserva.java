package com.calendario.agendarreservas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
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

    @Column(name = "abonado")
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

    public Reserva(Long idReserva, Cliente cliente, Timestamp fechaReserva, Timestamp fechaModificacion,
                   Timestamp fechaCreacion, EstadoReserva estado, Long precio,
                   Timestamp fechaTermino, String lugarEncuentro) {
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
}
