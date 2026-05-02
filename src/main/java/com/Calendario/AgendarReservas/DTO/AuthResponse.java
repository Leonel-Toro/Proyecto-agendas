package com.calendario.agendarreservas.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class AuthResponse {

    private boolean success;
    private String message;
    private UserInfo user;
    private String accessToken;
    private String refreshToken;

    public AuthResponse() {}

    public AuthResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public AuthResponse(boolean success, String message, UserInfo user) {
        this.success = success;
        this.message = message;
        this.user = user;
    }

    public AuthResponse(boolean success, String message, UserInfo user, String accessToken, String refreshToken) {
        this.success = success;
        this.message = message;
        this.user = user;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public static AuthResponse success(String message, UserInfo user) {
        return new AuthResponse(true, message, user);
    }

    public static AuthResponse success(String message) {
        return new AuthResponse(true, message);
    }

    public static AuthResponse successWithTokens(String message, UserInfo user, String accessToken, String refreshToken) {
        return new AuthResponse(true, message, user, accessToken, refreshToken);
    }

    public static AuthResponse successWithTokens(String message, String accessToken, String refreshToken) {
        AuthResponse response = new AuthResponse(true, message);
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        return response;
    }

    public static AuthResponse error(String message) {
        return new AuthResponse(false, message);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private Set<String> roles;
    }
}
