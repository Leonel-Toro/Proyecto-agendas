package com.Calendario.AgendarReservas.Service;

import com.Calendario.AgendarReservas.DTO.AuthResponse;
import com.Calendario.AgendarReservas.DTO.LoginRequest;
import com.Calendario.AgendarReservas.DTO.RegisterRequest;
import com.Calendario.AgendarReservas.Model.RefreshToken;
import com.Calendario.AgendarReservas.Model.Role;
import com.Calendario.AgendarReservas.Model.User;
import com.Calendario.AgendarReservas.Repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

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

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse response) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsernameOrEmail(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get user from database
            User user = userRepository.findByUsernameOrEmail(
                    request.getUsernameOrEmail(),
                    request.getUsernameOrEmail()
            ).orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

            // Update last login
            userRepository.updateLastLogin(user.getId());

            // Generate tokens
            String accessToken = jwtService.generateAccessToken(user);
            String userAgent = httpRequest.getHeader("User-Agent");
            String ipAddress = getClientIpAddress(httpRequest);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user, userAgent, ipAddress);

            // Set cookies
            addTokenCookies(response, accessToken, refreshToken.getToken());

            logger.info("Usuario {} inició sesión exitosamente desde IP: {}", user.getUsername(), ipAddress);

            return AuthResponse.success("Inicio de sesión exitoso", createUserInfo(user));

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

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if username exists
        if (userRepository.existsByUsername(request.getUsername())) {
            return AuthResponse.error("El nombre de usuario ya está en uso");
        }

        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            return AuthResponse.error("El email ya está registrado");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Set.of(Role.ROLE_USER));
        user.setEnabled(true);
        user.setAccountNonLocked(true);

        userRepository.save(user);

        logger.info("Nuevo usuario registrado: {}", user.getUsername());

        return AuthResponse.success("Usuario registrado exitosamente");
    }

    @Transactional
    public AuthResponse logout(HttpServletRequest request, HttpServletResponse response) {
        // Get refresh token from cookie
        String refreshToken = extractTokenFromCookie(request, REFRESH_TOKEN_COOKIE);

        if (refreshToken != null) {
            refreshTokenService.revokeToken(refreshToken);
        }

        // Clear security context
        SecurityContextHolder.clearContext();

        // Clear cookies
        clearTokenCookies(response);

        logger.info("Usuario cerró sesión exitosamente");

        return AuthResponse.success("Sesión cerrada exitosamente");
    }

    @Transactional(readOnly = true)
    public AuthResponse checkSession(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
            "anonymousUser".equals(authentication.getPrincipal())) {
            return AuthResponse.error("No hay sesión activa");
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return AuthResponse.error("Usuario no encontrado");
        }

        return AuthResponse.success("Sesión activa", createUserInfo(user));
    }

    @Transactional
    public AuthResponse refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshTokenValue = extractTokenFromCookie(request, REFRESH_TOKEN_COOKIE);

        if (refreshTokenValue == null) {
            return AuthResponse.error("Refresh token no encontrado");
        }

        try {
            RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenValue)
                    .orElseThrow(() -> new RuntimeException("Refresh token inválido"));

            refreshTokenService.verifyExpiration(refreshToken);

            User user = refreshToken.getUser();

            // Generate new access token
            String newAccessToken = jwtService.generateAccessToken(user);

            // Optionally rotate refresh token for security
            String userAgent = request.getHeader("User-Agent");
            String ipAddress = getClientIpAddress(request);
            RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user, userAgent, ipAddress);

            // Set new cookies
            addTokenCookies(response, newAccessToken, newRefreshToken.getToken());

            return AuthResponse.success("Token actualizado", createUserInfo(user));

        } catch (Exception e) {
            logger.error("Error al refrescar token: {}", e.getMessage());
            clearTokenCookies(response);
            return AuthResponse.error("Error al refrescar la sesión. Por favor, inicie sesión nuevamente.");
        }
    }

    private void addTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        // Access token cookie
        Cookie accessCookie = new Cookie(ACCESS_TOKEN_COOKIE, accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(secureCookie);
        accessCookie.setPath("/");
        accessCookie.setMaxAge((int) (accessTokenExpirationMs / 1000));
        if (!cookieDomain.isEmpty()) {
            accessCookie.setDomain(cookieDomain);
        }

        // Refresh token cookie
        Cookie refreshCookie = new Cookie(REFRESH_TOKEN_COOKIE, refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(secureCookie);
        refreshCookie.setPath("/api/auth"); // Only sent to auth endpoints
        refreshCookie.setMaxAge((int) (refreshTokenExpirationMs / 1000));
        if (!cookieDomain.isEmpty()) {
            refreshCookie.setDomain(cookieDomain);
        }

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        // Add SameSite attribute via header (not directly supported in Cookie class)
        response.addHeader("Set-Cookie",
                String.format("%s=%s; Path=/; HttpOnly; %s; SameSite=%s; Max-Age=%d",
                        ACCESS_TOKEN_COOKIE, accessToken,
                        secureCookie ? "Secure" : "",
                        sameSite,
                        accessTokenExpirationMs / 1000));
        response.addHeader("Set-Cookie",
                String.format("%s=%s; Path=/api/auth; HttpOnly; %s; SameSite=%s; Max-Age=%d",
                        REFRESH_TOKEN_COOKIE, refreshToken,
                        secureCookie ? "Secure" : "",
                        sameSite,
                        refreshTokenExpirationMs / 1000));
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

        return new AuthResponse.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roles
        );
    }
}

