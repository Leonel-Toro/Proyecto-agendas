package com.Calendario.AgendarReservas.Repository;

import com.Calendario.AgendarReservas.Model.RefreshToken;
import com.Calendario.AgendarReservas.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByTokenAndRevokedFalse(String token);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user = :user")
    void revokeAllUserTokens(@Param("user") User user);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user.id = :userId")
    void revokeAllUserTokensById(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    void deleteAllExpiredTokens(@Param("now") Instant now);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user = :user AND rt.revoked = true")
    void deleteRevokedTokensByUser(@Param("user") User user);

    long countByUserAndRevokedFalseAndExpiryDateAfter(User user, Instant now);
}

