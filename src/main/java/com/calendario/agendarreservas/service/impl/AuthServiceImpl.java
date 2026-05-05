package com.calendario.agendarreservas.service.impl;

import com.calendario.agendarreservas.dto.AuthResponse;
import com.calendario.agendarreservas.dto.LoginRequest;
import com.calendario.agendarreservas.dto.RegisterRequest;
import com.calendario.agendarreservas.model.Genero;
import com.calendario.agendarreservas.model.RefreshToken;
import com.calendario.agendarreservas.model.Role;
import com.calendario.agendarreservas.model.User;
import com.calendario.agendarreservas.repository.UserRepository;
import com.calendario.agendarreservas.service.AuthService;
import com.calendario.agendarreservas.service.JwtService;
import com.calendario.agendarreservas.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final String ACCESS_TOKEN_COOKIE = "access_token";
    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;

    @Value("${app.jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    @Value("${app.cookie.secure:true}")
    private boolean secureCookie;

    @Value("${app.cookie.same-site:None}")
    private String sameSite;

    @Value("${app.cookie.domain:}")
    private String cookieDomain;

    @Override
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userRepository.findByUsernameOrEmail(
                    request.getUsernameOrEmail(), request.getUsernameOrEmail()
            ).orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

            userRepository.updateLastLogin(user.getId());

            String accessToken = jwtService.generateAccessToken(user);
            String userAgent = httpRequest.getHeader("User-Agent");
            String ipAddress = getClientIpAddress(httpRequest);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user, userAgent, ipAddress);

            addTokenCookies(response, accessToken, refreshToken.getToken());
            logger.info("Usuario {} inició sesión exitosamente desde IP: {}", user.getUsername(), ipAddress);

            return AuthResponse.successWithTokens("Inicio de sesión exitoso", createUserInfo(user), accessToken, refreshToken.getToken());

        } catch (BadCredentialsException e) {
            logger.warn("Intento de login fallido para: {}", request.getUsernameOrEmail());
            return AuthResponse.error("Credenciales inválidas");
        } catch (io.jsonwebtoken.security.WeakKeyException e) {
            logger.error("Error de configuración JWT - clave demasiado débil: {}", e.getMessage());
            return AuthResponse.error("Error de configuración del servidor. Contacte al administrador.");
        } catch (Exception e) {
            logger.error("Error durante el login: {}", e.getMessage());
            return AuthResponse.error("Error durante el inicio de sesión");
        }
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest, HttpServletResponse response) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return AuthResponse.error("El nombre de usuario ya está en uso");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            return AuthResponse.error("El email ya está registrado");
        }
        if (request.getRut() != null && !request.getRut().isBlank()
                && userRepository.existsByRut(request.getRut())) {
            return AuthResponse.error("El RUT ya está registrado");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Set.of(Role.ROLE_USER));
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        user.setNombre(request.getNombre());
        user.setApellidos(request.getApellidos());
        user.setRut(request.getRut());
        user.setTelefono(request.getTelefono());
        user.setFechaNacimiento(request.getFechaNacimiento());
        user.setEdad(request.getEdad());
        if (request.getGenero() != null && !request.getGenero().isBlank()) {
            user.setGenero(Genero.valueOf(request.getGenero().toUpperCase()));
        }
        user.setPacienteAnterior(request.getPacienteAnterior());
        user.setEstudiante(request.getEstudiante());
        userRepository.save(user);

        logger.info("Nuevo usuario registrado: {}", user.getUsername());

        String accessToken = jwtService.generateAccessToken(user);
        String userAgent = httpRequest.getHeader("User-Agent");
        String ipAddress = getClientIpAddress(httpRequest);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user, userAgent, ipAddress);

        addTokenCookies(response, accessToken, refreshToken.getToken());

        return AuthResponse.successWithTokens("Usuario registrado exitosamente", createUserInfo(user), accessToken, refreshToken.getToken());
    }

    @Override
    public AuthResponse logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractTokenFromCookie(request, REFRESH_TOKEN_COOKIE);
        if (refreshToken != null) {
            refreshTokenService.revokeToken(refreshToken);
        }
        SecurityContextHolder.clearContext();
        clearTokenCookies(response);
        logger.info("Usuario cerró sesión exitosamente");
        return AuthResponse.success("Sesión cerrada exitosamente");
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse checkSession(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            return AuthResponse.error("No hay sesión activa");
        }

        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user == null) {
            return AuthResponse.error("Usuario no encontrado");
        }

        return AuthResponse.success("Sesión activa", createUserInfo(user));
    }

    @Override
    public AuthResponse refreshAccessToken(HttpServletRequest request, HttpServletResponse response, String bodyRefreshToken) {
        String refreshTokenValue = null;

        if (bodyRefreshToken != null && !bodyRefreshToken.isEmpty()) {
            refreshTokenValue = bodyRefreshToken;
            logger.debug("Refresh token obtenido del body");
        }
        if (refreshTokenValue == null) {
            refreshTokenValue = extractTokenFromHeader(request);
            if (refreshTokenValue != null) logger.debug("Refresh token obtenido del header Authorization");
        }
        if (refreshTokenValue == null) {
            refreshTokenValue = extractTokenFromCookie(request, REFRESH_TOKEN_COOKIE);
            if (refreshTokenValue != null) logger.debug("Refresh token obtenido de la cookie");
        }

        if (refreshTokenValue == null) {
            return AuthResponse.error("Refresh token no encontrado");
        }

        try {
            RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenValue)
                    .orElseThrow(() -> new RuntimeException("Refresh token inválido"));

            refreshTokenService.verifyExpiration(refreshToken);

            User user = refreshToken.getUser();
            String newAccessToken = jwtService.generateAccessToken(user);
            String userAgent = request.getHeader("User-Agent");
            String ipAddress = getClientIpAddress(request);
            RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user, userAgent, ipAddress);

            addTokenCookies(response, newAccessToken, newRefreshToken.getToken());

            return AuthResponse.successWithTokens("Token actualizado", createUserInfo(user), newAccessToken, newRefreshToken.getToken());

        } catch (Exception e) {
            logger.error("Error al refrescar token: {}", e.getMessage());
            clearTokenCookies(response);
            return AuthResponse.error("Error al refrescar la sesión. Por favor, inicie sesión nuevamente.");
        }
    }

    private void addTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        Cookie accessCookie = new Cookie(ACCESS_TOKEN_COOKIE, accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(secureCookie);
        accessCookie.setPath("/");
        accessCookie.setMaxAge((int) (accessTokenExpirationMs / 1000));
        if (!cookieDomain.isEmpty()) accessCookie.setDomain(cookieDomain);

        Cookie refreshCookie = new Cookie(REFRESH_TOKEN_COOKIE, refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(secureCookie);
        refreshCookie.setPath("/api/auth");
        refreshCookie.setMaxAge((int) (refreshTokenExpirationMs / 1000));
        if (!cookieDomain.isEmpty()) refreshCookie.setDomain(cookieDomain);

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        response.addHeader("Set-Cookie",
                String.format("%s=%s; Path=/; HttpOnly; %s; SameSite=%s; Max-Age=%d",
                        ACCESS_TOKEN_COOKIE, accessToken,
                        secureCookie ? "Secure" : "",
                        sameSite, accessTokenExpirationMs / 1000));
        response.addHeader("Set-Cookie",
                String.format("%s=%s; Path=/api/auth; HttpOnly; %s; SameSite=%s; Max-Age=%d",
                        REFRESH_TOKEN_COOKIE, refreshToken,
                        secureCookie ? "Secure" : "",
                        sameSite, refreshTokenExpirationMs / 1000));
    }

    private void clearTokenCookies(HttpServletResponse response) {
        Cookie accessCookie = new Cookie(ACCESS_TOKEN_COOKIE, "");
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(secureCookie);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(0);

        Cookie refreshCookie = new Cookie(REFRESH_TOKEN_COOKIE, "");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(secureCookie);
        refreshCookie.setPath("/api/auth");
        refreshCookie.setMaxAge(0);

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);
    }

    @Override
    public String extractTokenFromCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    @Override
    public String extractTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    private AuthResponse.UserInfo createUserInfo(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
        return new AuthResponse.UserInfo(user.getId(), user.getUsername(), user.getEmail(), roles);
    }
}
