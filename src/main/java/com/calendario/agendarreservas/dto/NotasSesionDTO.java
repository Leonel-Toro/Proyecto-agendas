package com.calendario.agendarreservas.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotasSesionDTO {

    private Long idNota;
    private Long idHistorial;

    @NotBlank(message = "La nota no puede estar vacía")
    private String nota;

    private Instant fechaCreacion;
}
