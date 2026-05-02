package com.calendario.agendarreservas.service;

import com.calendario.agendarreservas.model.User;
import io.jsonwebtoken.Claims;

import java.util.Date;
import java.util.function.Function;

public interface JwtService {

    String generateAccessToken(User user);

    String generateRefreshToken(User user);

    String extractUsername(String token);

    Long extractUserId(String token);

    Date extractExpiration(String token);

    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);

    boolean validateToken(String token);

    boolean isTokenExpired(String token);

    long getAccessTokenExpirationMs();

    long getRefreshTokenExpirationMs();
}
