package com.calendario.agendarreservas.dto;

import com.calendario.agendarreservas.validation.ValidRut;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "El nombre de usuario es requerido")
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
    private String username;

    @NotBlank(message = "El email es requerido")
    @Email(message = "El email debe ser válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String email;

    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 8, max = 120, message = "La contraseña debe tener entre 8 y 120 caracteres")
    private String password;

    @NotBlank(message = "El nombre es requerido")
    @Size(max = 100)
    private String nombre;

    @NotBlank(message = "Los apellidos son requeridos")
    @Size(max = 150)
    private String apellidos;

    @ValidRut
    private String rut;

    @Size(max = 20)
    private String telefono;

    private LocalDate fechaNacimiento;

    private Integer edad;

    private String genero;

    private Boolean pacienteAnterior;

    private Boolean estudiante;
}
