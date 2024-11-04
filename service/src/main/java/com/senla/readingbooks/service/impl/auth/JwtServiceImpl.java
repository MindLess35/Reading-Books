package com.senla.readingbooks.service.impl.auth;

import com.senla.readingbooks.entity.user.User;
import com.senla.readingbooks.enums.user.TokenType;
import com.senla.readingbooks.property.JwtProperty;
import com.senla.readingbooks.service.interfaces.auth.JwtService;
import com.senla.readingbooks.util.JwtDeserializer;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
    private final JwtProperty jwtProperty;
    private final JwtDeserializer jwtDeserializer;
    private SecretKey signingKey;
    private SecretKey encryptionKey;

    @PostConstruct
    private void initSecretKey() {
        signingKey = getSigningSecretKey();
        encryptionKey = getEncryptionSecretKey();
    }

    @Override
    public String generateAccessToken(User user, UUID jti) {
        Map<String, ? extends Serializable> claims =
                Map.of(JTI_UUID, jti,
                        TOKEN_TYPE, TokenType.ACCESS,
                        ROLE, user.getRole());
        return buildToken(claims, user.getId(), jwtProperty.getAccessExpiration());
    }

    @Override
    public String generateRefreshToken(Long userId, UUID jti, String deviceId) {
        Map<String, ? extends Serializable> claims =
                Map.of(JTI_UUID, jti,
                        TOKEN_TYPE, TokenType.REFRESH,
                        DEVICE_ID, deviceId);
        return buildToken(claims, userId, jwtProperty.getRefreshExpiration());
    }

    private String buildToken(Map<String, ? extends Serializable> claims, Long userId, Long expiration) {
        String signedToken = Jwts
                .builder()
                .claims(claims)
                .subject(userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();

        return encryptToken(signedToken);
    }

    private String encryptToken(String signedToken) {
        return Jwts.builder()
                .claim(SIGNED_TOKEN, signedToken)
                .encryptWith(encryptionKey, Jwts.ENC.A256CBC_HS512)
                .compact();
    }

    @Override
    public boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    @Override
    public TokenType extractTokenType(Claims claims) {
        return claims.get(TOKEN_TYPE, TokenType.class);
    }

    @Override
    public Long extractSubject(Claims claims) {
        return Long.valueOf(claims.getSubject());
    }

    @Override
    public UUID extractJti(Claims claims) {
        return claims.get(JTI_UUID, UUID.class);
    }

    @Override
    public boolean isTokenHasType(Claims claims, TokenType tokenType) {
        return extractTokenType(claims).equals(tokenType);
    }

    @Override
    public Claims extractAllClaims(String encryptedToken) {
        try {
            String token = decryptToken(encryptedToken);
            return Jwts
                    .parser()
                    .verifyWith(signingKey)
                    .json(jwtDeserializer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (IllegalArgumentException ex) {
            throw new JwtException("Token is invalid", ex);
        }
    }

    private String decryptToken(String encryptedToken) {
        return Jwts
                .parser()
                .decryptWith(encryptionKey)
                .build()
                .parseEncryptedClaims(encryptedToken)
                .getPayload()
                .get(SIGNED_TOKEN, String.class);
    }

    private SecretKey getSigningSecretKey() {
        return Keys.hmacShaKeyFor(jwtProperty.getSigningKey().getBytes());
    }

    private SecretKey getEncryptionSecretKey() {
        return Keys.hmacShaKeyFor(jwtProperty.getEncryptionKey().getBytes());
    }
}
