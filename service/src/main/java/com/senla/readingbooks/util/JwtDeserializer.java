package com.senla.readingbooks.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.senla.readingbooks.enums.user.Role;
import com.senla.readingbooks.enums.user.TokenType;
import com.senla.readingbooks.service.interfaces.auth.JwtService;
import io.jsonwebtoken.io.DeserializationException;
import io.jsonwebtoken.io.Deserializer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtDeserializer implements Deserializer<Map<String, ?>> {
    private final ObjectMapper objectMapper;

    @Override
    public Map<String, ?> deserialize(byte[] bytes) throws DeserializationException {
        try {
            return deserializeClaims(objectMapper.readValue(bytes, Map.class));
        } catch (IOException e) {
            throw new DeserializationException("Error deserializing claims", e);
        }
    }

    @Override
    public Map<String, ?> deserialize(Reader reader) throws DeserializationException {
        try {
            return deserializeClaims(objectMapper.readValue(reader, Map.class));
        } catch (IOException e) {
            throw new DeserializationException("Error deserializing claims", e);

        }
    }

    private Map<String, Object> deserializeClaims(Map<String, Object> claims) {

        if (claims.containsKey(JwtService.TOKEN_TYPE)) {
            String tokenType = (String) claims.get(JwtService.TOKEN_TYPE);
            claims.put(JwtService.TOKEN_TYPE, TokenType.valueOf(tokenType));
        }

        if (claims.containsKey(JwtService.JTI_UUID)) {
            String jti = (String) claims.get(JwtService.JTI_UUID);
            claims.put(JwtService.JTI_UUID, UUID.fromString(jti));
        }

        if (claims.containsKey(JwtService.ROLE)) {
            String role = (String) claims.get(JwtService.ROLE);
            claims.put(JwtService.ROLE, Role.valueOf(role));
        }
        return claims;
    }
}