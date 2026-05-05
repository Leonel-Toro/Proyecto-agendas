package com.calendario.agendarreservas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.Instant;

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

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paciente", nullable = false)
    private User paciente;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_psicologo", nullable = false)
    private User psicologo;

    @NotNull
    @Column(name = "fecha_reserva", nullable = false)
    private Timestamp fechaReserva;

    @Column(name = "fecha_termino")
    private Timestamp fechaTermino;

    // Duración en minutos: múltiplo de 30, entre 30 y 360. Default 60.
    @Column(name = "duracion_minutos", nullable = false)
    private Integer duracionMinutos = 60;

    @Column(name = "motivo_consulta", length = 500)
    private String motivoConsulta;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "modalidad", nullable = false, length = 20)
    private Modalidad modalidad;

    @NotNull
    @Column(name = "precio", nullable = false)
    private Long precio = 0L;

    @Column(name = "abonado")
    private Long abonado = 0L;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoReserva estado = EstadoReserva.PENDIENTE;

    // Historial clínico vinculado a esta sesión (creado por el psicólogo)
    @OneToOne(mappedBy = "reserva", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private HistorialPaciente historialPaciente;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    @Column(name = "fecha_modificacion")
    private Instant fechaModificacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = Instant.now();
        fechaModificacion = Instant.now();
        if (estado == null) estado = EstadoReserva.PENDIENTE;
        if (duracionMinutos == null) duracionMinutos = 60;
    }

    @PreUpdate
    protected void onUpdate() {
        fechaModificacion = Instant.now();
    }
}
