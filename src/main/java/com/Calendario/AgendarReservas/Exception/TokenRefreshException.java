package com.calendario.agendarreservas.exception;

public class TokenRefreshException extends RuntimeException {

    public TokenRefreshException(String token, String message) {
        super(String.format("Error con token [%s]: %s", token, message));
    }

    public TokenRefreshException(String message) {
        super(message);
    }
}

