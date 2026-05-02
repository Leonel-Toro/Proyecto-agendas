package com.calendario.agendarreservas.service;

import com.calendario.agendarreservas.model.RefreshToken;
import com.calendario.agendarreservas.model.User;

import java.util.Optional;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user, String userAgent, String ipAddress);

    Optional<RefreshToken> findByToken(String token);

    RefreshToken verifyExpiration(RefreshToken token);

    void revokeToken(String token);

    void revokeAllUserTokens(User user);

    void revokeAllUserTokensById(Long userId);
}
