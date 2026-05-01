package com.Calendario.AgendarReservas.Security;

import com.Calendario.AgendarReservas.Service.CustomUserDetailsService;
import com.Calendario.AgendarReservas.Service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String ACCESS_TOKEN_COOKIE = "access_token";

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = extractTokenFromHeader(request);

            if (jwt != null) {
                logger.debug("Token encontrado en header Authorization para: {}", request.getRequestURI());
            } else {
                jwt = extractTokenFromCookie(request);
                if (jwt != null) {
                    logger.debug("Token encontrado en cookie access_token para: {}", request.getRequestURI());
                } else {
                    logger.debug("No se encontró token para: {}", request.getRequestURI());
                }
            }

            if (jwt != null && jwtService.validateToken(jwt)) {
                String username = jwtService.extractUsername(jwt);
                logger.debug("Token válido para usuario: {}", username);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (!jwtService.isTokenExpired(jwt)) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        authentication.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        logger.debug("Autenticación establecida para: {}", username);
                    }
                }
            } else if (jwt != null) {
                logger.warn("Token JWT inválido o expirado para: {}", request.getRequestURI());
            }
        } catch (Exception e) {
            logger.error("Error al procesar autenticación JWT: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (ACCESS_TOKEN_COOKIE.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // Don't filter public auth endpoints
        return path.equals("/api/auth/login") || path.equals("/api/auth/register");
    }
}

