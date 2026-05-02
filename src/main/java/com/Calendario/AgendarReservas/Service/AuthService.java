package com.calendario.agendarreservas.service;

import com.calendario.agendarreservas.dto.AuthResponse;
import com.calendario.agendarreservas.dto.LoginRequest;
import com.calendario.agendarreservas.dto.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    AuthResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse response);

    AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest, HttpServletResponse response);

    AuthResponse logout(HttpServletRequest request, HttpServletResponse response);

    AuthResponse checkSession(HttpServletRequest request, HttpServletResponse response);

    AuthResponse refreshAccessToken(HttpServletRequest request, HttpServletResponse response, String bodyRefreshToken);

    String extractTokenFromCookie(HttpServletRequest request, String cookieName);

    String extractTokenFromHeader(HttpServletRequest request);
}
