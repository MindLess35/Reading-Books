package com.senla.readingbooks.repository.jpa.user;

import com.senla.readingbooks.entity.user.RefreshToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TokenRepository extends JpaRepository<RefreshToken, Long> {

    @Modifying
    @Query(value = """
            UPDATE refresh_tokens
            SET is_revoked = true
            WHERE user_id = :userId
            RETURNING jti
            """, nativeQuery = true)
    List<UUID> revokeAllUserTokens(Long userId);

    @Modifying
    @Query(value = """
            UPDATE RefreshToken t
            SET t.isRevoked = true
            WHERE t.jti = :jti AND t.isRevoked = false
            """)
    int revokeTokenByJti(UUID jti);

    @Query(value = """
            UPDATE refresh_tokens t
            SET is_revoked = true
            WHERE t.user_id = :id
                  AND t.device_id = :deviceId
                  AND t.is_revoked = false
            RETURNING t.jti
            """, nativeQuery = true)
    UUID revokeTokenByUserIdAndDeviceId(Long id, String deviceId);

    Optional<RefreshToken> findByJtiAndDeviceId(UUID jti, String deviceId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            DELETE FROM RefreshToken rt
            WHERE rt.expiresAt < :now
            """)
    void deleteExpiredTokens(Instant now);
}

