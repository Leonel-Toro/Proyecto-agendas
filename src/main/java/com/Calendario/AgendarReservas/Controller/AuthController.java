package com.calendario.agendarreservas.controller;

import com.calendario.agendarreservas.dto.AuthResponse;
import com.calendario.agendarreservas.dto.LoginRequest;
import com.calendario.agendarreservas.dto.RefreshTokenRequest;
import com.calendario.agendarreservas.dto.RegisterRequest;
import com.calendario.agendarreservas.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Login endpoint - Public
     * POST /api/auth/login
     * Returns tokens in both cookies and response body for Safari/iOS compatibility
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response) {

        AuthResponse authResponse = authService.login(request, httpRequest, response);

        if (authResponse.isSuccess()) {
            return ResponseEntity.ok(authResponse);
        }

        return ResponseEntity.status(401).body(authResponse);
    }

    /**
     * Register endpoint - Public
     * POST /api/auth/register
     * Returns tokens in both cookies and response body for Safari/iOS compatibility
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response) {

        AuthResponse authResponse = authService.register(request, httpRequest, response);

        if (authResponse.isSuccess()) {
            return ResponseEntity.status(201).body(authResponse);
        }

        return ResponseEntity.badRequest().body(authResponse);
    }

    /**
     * Logout endpoint - Private (requires authentication)
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        AuthResponse authResponse = authService.logout(request, response);
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Check session endpoint - Private (requires authentication)
     * GET /api/auth/check-session
     * Accepts access token from: 1. Authorization header, 2. Cookie
     * For Safari/iOS compatibility when cookies are blocked by ITP
     */
    @GetMapping("/check-session")
    public ResponseEntity<AuthResponse> checkSession(
            HttpServletRequest request,
            HttpServletResponse response) {

        AuthResponse authResponse = authService.checkSession(request, response);

        if (authResponse.isSuccess()) {
            return ResponseEntity.ok(authResponse);
        }

        return ResponseEntity.status(401).body(authResponse);
    }

    /**
     * Refresh token endpoint - Public (but requires valid refresh token)
     * POST /api/auth/refresh
     * Accepts refresh token from: 1. Request body, 2. Authorization header, 3. Cookie
     * Returns new tokens in both cookies and response body for Safari/iOS compatibility
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @RequestBody(required = false) RefreshTokenRequest refreshTokenRequest,
            HttpServletRequest request,
            HttpServletResponse response) {

        String bodyRefreshToken = refreshTokenRequest != null ? refreshTokenRequest.getRefreshToken() : null;
        AuthResponse authResponse = authService.refreshAccessToken(request, response, bodyRefreshToken);

        if (authResponse.isSuccess()) {
            return ResponseEntity.ok(authResponse);
        }

        return ResponseEntity.status(401).body(authResponse);
    }

    /**
     * Get CSRF token - for SPA frontend
     * GET /api/auth/csrf
     */
    @GetMapping("/csrf")
    public ResponseEntity<CsrfTokenResponse> getCsrfToken(CsrfToken csrfToken) {
        return ResponseEntity.ok(new CsrfTokenResponse(
                csrfToken.getToken(),
                csrfToken.getHeaderName()
        ));
    }

    // Inner class for CSRF response
    public record CsrfTokenResponse(String token, String headerName) {}
}

