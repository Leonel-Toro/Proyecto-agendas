package com.Calendario.AgendarReservas.Controller;

import com.Calendario.AgendarReservas.DTO.AuthResponse;
import com.Calendario.AgendarReservas.DTO.LoginRequest;
import com.Calendario.AgendarReservas.DTO.RegisterRequest;
import com.Calendario.AgendarReservas.Service.AuthService;
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
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse authResponse = authService.register(request);

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
     * Refresh token endpoint - Public (but requires valid refresh token in cookie)
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {

        AuthResponse authResponse = authService.refreshAccessToken(request, response);

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

