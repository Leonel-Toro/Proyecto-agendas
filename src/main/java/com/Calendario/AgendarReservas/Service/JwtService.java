package com.Calendario.AgendarReservas.Service;

import com.Calendario.AgendarReservas.Config.JwtSecretConfig;
import com.Calendario.AgendarReservas.Model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    private final JwtSecretConfig jwtSecretConfig;

    @Value("${app.jwt.secret:}")
    private String configuredSecret;

    @Value("${app.jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    @Value("${app.jwt.issuer}")
    private String issuer;

    public JwtService(JwtSecretConfig jwtSecretConfig) {
        this.jwtSecretConfig = jwtSecretConfig;
    }

    private SecretKey getSigningKey() {
        String secret = getEffectiveSecret();
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String getEffectiveSecret() {
        // Prioridad: Variable de entorno > Configuración > JwtSecretConfig
        String envSecret = System.getenv("JWT_SECRET");
        if (envSecret != null && !envSecret.isBlank()) {
            return envSecret;
        }
        if (configuredSecret != null && !configuredSecret.isBlank()) {
            return configuredSecret;
        }
        return jwtSecretConfig.getEffectiveSecret();
    }

    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("roles", user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toList()));

        return buildToken(claims, user.getUsername(), accessTokenExpirationMs);
    }

    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("type", "refresh");

        return buildToken(claims, user.getUsername(), refreshTokenExpirationMs);
    }

    private String buildToken(Map<String, Object> claims, String subject, long expirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (MalformedJwtException ex) {
            logger.error("Token JWT inválido: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("Token JWT expirado: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("Token JWT no soportado: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("Claims JWT vacío: {}", ex.getMessage());
        } catch (JwtException ex) {
            logger.error("Error de validación JWT: {}", ex.getMessage());
        }
        return false;
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException ex) {
            return true;
        }
    }

    public long getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }

    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }
}

