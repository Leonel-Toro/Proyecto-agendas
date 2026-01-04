package com.Calendario.AgendarReservas.DTO;

import java.util.Set;

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

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // Inner class for safe user info (no sensitive data)
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private Set<String> roles;

        public UserInfo() {}

        public UserInfo(Long id, String username, String email, Set<String> roles) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.roles = roles;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public Set<String> getRoles() {
            return roles;
        }

        public void setRoles(Set<String> roles) {
            this.roles = roles;
        }
    }
}

