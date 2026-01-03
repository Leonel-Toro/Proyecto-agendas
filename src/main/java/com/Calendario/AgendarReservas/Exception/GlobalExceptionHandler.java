package com.Calendario.AgendarReservas.Exception;

import com.Calendario.AgendarReservas.DTO.AuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null ?
                                fieldError.getDefaultMessage() : "Error de validación",
                        (existing, replacement) -> existing
                ));

        ErrorResponse response = new ErrorResponse(
                false,
                HttpStatus.BAD_REQUEST.value(),
                "Error de validación",
                "Los datos proporcionados no son válidos",
                errors,
                Instant.now()
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<AuthResponse> handleBadCredentials(BadCredentialsException ex) {
        logger.warn("Intento de autenticación fallido: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(AuthResponse.error("Credenciales inválidas"));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<AuthResponse> handleAuthenticationException(AuthenticationException ex) {
        logger.error("Error de autenticación: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(AuthResponse.error("Error de autenticación"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<AuthResponse> handleAccessDenied(AccessDeniedException ex) {
        logger.warn("Acceso denegado: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(AuthResponse.error("No tiene permisos para acceder a este recurso"));
    }

    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<AuthResponse> handleTokenRefreshException(TokenRefreshException ex) {
        logger.error("Error de refresh token: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(AuthResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<AuthResponse> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(AuthResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        logger.error("Error no manejado: ", ex);

        ErrorResponse response = new ErrorResponse(
                false,
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno",
                "Ha ocurrido un error inesperado. Por favor, intente más tarde.",
                null,
                Instant.now()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // Error response record
    public record ErrorResponse(
            boolean success,
            int status,
            String error,
            String message,
            Map<String, String> details,
            Instant timestamp
    ) {}
}

