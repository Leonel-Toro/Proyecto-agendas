package com.calendario.agendarreservas.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;
    private String username;
    private String email;
    private String nombre;
    private String apellidos;
    private String rut;
    private String telefono;
    private LocalDate fechaNacimiento;
    private Integer edad;
    private String genero;
    private Boolean pacienteAnterior;
    private Boolean estudiante;
    private boolean enabled;
    private Instant createdAt;
    private Instant lastLogin;
}
