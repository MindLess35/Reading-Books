package com.senla.readingbooks.service.interfaces.auth;

import com.senla.readingbooks.entity.user.User;
import io.jsonwebtoken.Claims;

import java.util.List;
import java.util.UUID;

public interface TokenService {

    void saveToken(User user, UUID jti, String deviceId);

    void revokeTokenByJti(Claims claims, Long userId, String deviceId);

    void revokeTokenByJti(Claims claims);

    void addJtiToRedisBlackList(List<UUID> jti);

    void revokeTokenByUserIdAndDeviceId(Long userId, String deviceId);

    void revokeTokenByUserIdAndDeviceIdSoft(Long userId, String deviceId);

    void revokeAllUserTokensById(Long id);
}
