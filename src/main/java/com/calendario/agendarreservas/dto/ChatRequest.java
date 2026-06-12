package com.calendario.agendarreservas.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        @NotBlank(message = "El sessionId es obligatorio") String sessionId,
        @NotBlank(message = "El mensaje no puede estar vacío") String message
) {}
