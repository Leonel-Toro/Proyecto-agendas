package com.calendario.agendarreservas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "historial_paciente")
public class HistorialPaciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial")
    private Long idHistorial;

    // Sesión (reserva) a la que corresponde este registro
    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reserva", nullable = false, unique = true)
    private Reserva reserva;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paciente", nullable = false)
    private User paciente;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_psicologo", nullable = false)
    private User psicologo;

    @Column(name = "motivo_consulta", length = 500)
    private String motivoConsulta;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_sesion", length = 20)
    private TipoSesion tipoSesion;

    @Column(name = "crisis")
    private Boolean crisis = false;

    @Column(name = "alta")
    private Boolean alta = false;

    @Column(name = "posible_abandono")
    private Boolean posibleAbandono = false;

    @Column(name = "notas_generales", columnDefinition = "TEXT")
    private String notasGenerales;

    @OneToMany(mappedBy = "historialPaciente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("fechaCreacion ASC")
    private List<NotasSesion> notasSesion = new ArrayList<>();

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private Instant fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = Instant.now();
        fechaActualizacion = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = Instant.now();
    }
}
