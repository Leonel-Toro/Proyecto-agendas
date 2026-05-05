package com.calendario.agendarreservas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "notas_sesion")
public class NotasSesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nota")
    private Long idNota;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_historial", nullable = false)
    private HistorialPaciente historialPaciente;

    @NotBlank
    @Column(name = "nota", columnDefinition = "TEXT", nullable = false)
    private String nota;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = Instant.now();
    }
}
