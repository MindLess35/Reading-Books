package com.senla.readingbooks.service.interfaces.auth;

import com.senla.readingbooks.entity.user.User;
import com.senla.readingbooks.enums.user.TokenType;
import io.jsonwebtoken.Claims;

import java.util.UUID;

public interface JwtService {
    String BEARER_ = "Bearer ";
    String DEVICE_ID = "deviceId";
    String SIGNED_TOKEN = "signedToken";
    String TOKEN_TYPE = "tokenType";
    String ROLE = "role";
    String JTI_UUID = "jtiUuid";

    Claims extractAllClaims(String token);

    String generateAccessToken(User user, UUID jti);

    String generateRefreshToken(Long userId, UUID jti, String deviceId);

    boolean isTokenExpired(Claims claims);

    TokenType extractTokenType(Claims claims);

    Long extractSubject(Claims claims);

    UUID extractJti(Claims claims);

    boolean isTokenHasType(Claims claims, TokenType tokenType);
}
