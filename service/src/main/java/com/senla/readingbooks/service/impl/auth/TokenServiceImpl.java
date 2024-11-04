package com.senla.readingbooks.service.impl.auth;

import com.senla.readingbooks.entity.user.RefreshToken;
import com.senla.readingbooks.entity.user.User;
import com.senla.readingbooks.exception.base.ResourceNotFoundException;
import com.senla.readingbooks.exception.security.AuthException;
import com.senla.readingbooks.property.CacheProperty;
import com.senla.readingbooks.property.JwtProperty;
import com.senla.readingbooks.repository.jpa.user.TokenRepository;
import com.senla.readingbooks.service.interfaces.auth.JwtService;
import com.senla.readingbooks.service.interfaces.auth.TokenService;
import io.jsonwebtoken.Claims;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TokenServiceImpl implements TokenService {
    private final TokenRepository tokenRepository;
    private final JwtProperty jwtProperty;
    private final JwtService jwtService;
    private final CacheProperty cacheProperty;
    private final RedisTemplate<String, String> redisTemplate;
    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public void saveToken(User user, UUID jti, String deviceId) {
        RefreshToken token = RefreshToken.builder()
                .expiresAt(Instant.now().plusMillis(jwtProperty.getRefreshExpiration()))
                .deviceId(deviceId)
                .user(user)
                .jti(jti)
                .build();
        entityManager.persist(token);
    }

    @Transactional(noRollbackFor = AuthException.class)
    @Override
    public void revokeTokenByJti(Claims claims, Long userId, String deviceId) {
        UUID extractedJti = jwtService.extractJti(claims);
        tokenRepository.findByJtiAndDeviceId(extractedJti, deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("A user with id %d does not have token on a %s device or is it a token for another device"
                        .formatted(userId, deviceId)));

        addJtiToRedisBlackList(List.of(extractedJti));
        int revokedCount = revokeAndCounting(claims);
        if (revokedCount == 0) {
            List<UUID> jtis = tokenRepository.revokeAllUserTokens(userId);
            addJtiToRedisBlackList(jtis);
            log.warn("Threat to the security of a user with id " + userId);
            throw new AuthException("Trying to use the refresh token twice - someone could have stolen it.");
        }
    }

    @Override
    public void revokeTokenByJti(Claims claims) {
        int revokedCount = revokeAndCounting(claims);
        if (revokedCount == 0) {
            throw new ResourceNotFoundException("Token has already been revoked");
        }
        addJtiToRedisBlackList(List.of(jwtService.extractJti(claims)));
    }

    @Override
    public void addJtiToRedisBlackList(List<UUID> jtis) {
        if (jtis.isEmpty())
            return;
        double expirationTime = (double) System.currentTimeMillis() + jwtProperty.getAccessExpiration();
        Set<ZSetOperations.TypedTuple<String>> tuples = new HashSet<>();

        jtis.stream()
                .map(UUID::toString)
                .map(jti -> new DefaultTypedTuple<>(jti, expirationTime))
                .forEach(tuples::add);
        if (!jtis.isEmpty()) {
            redisTemplate.opsForZSet().addIfAbsent(cacheProperty.getJtiBlackListName(), tuples);
        }
    }

    @Override
    public void revokeTokenByUserIdAndDeviceId(Long userId, String deviceId) {
        UUID jti = tokenRepository.revokeTokenByUserIdAndDeviceId(userId, deviceId);
        if (jti == null)
            throw new ResourceNotFoundException("A user with id %d does not have tokens on a %s device".formatted(userId, deviceId));
        addJtiToRedisBlackList(List.of(jti));
    }

    @Override
    public void revokeTokenByUserIdAndDeviceIdSoft(Long userId, String deviceId) {
        UUID jti = tokenRepository.revokeTokenByUserIdAndDeviceId(userId, deviceId);
        if (jti != null) {
            addJtiToRedisBlackList(List.of(jti));
        }
    }

    private int revokeAndCounting(Claims claims) {
        UUID extractedJti = jwtService.extractJti(claims);
        return tokenRepository.revokeTokenByJti(extractedJti);
    }

    @Override
    public void revokeAllUserTokensById(Long userId) {
        List<UUID> jtis = tokenRepository.revokeAllUserTokens(userId);
        addJtiToRedisBlackList(jtis);
    }

}

