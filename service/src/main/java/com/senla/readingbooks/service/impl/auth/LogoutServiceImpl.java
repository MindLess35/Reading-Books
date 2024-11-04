package com.senla.readingbooks.service.impl.auth;

import com.senla.readingbooks.enums.user.TokenType;
import com.senla.readingbooks.exception.base.BadRequestBaseException;
import com.senla.readingbooks.service.interfaces.auth.JwtService;
import com.senla.readingbooks.service.interfaces.auth.LogoutService;
import com.senla.readingbooks.service.interfaces.auth.TokenService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.senla.readingbooks.service.interfaces.auth.JwtService.BEARER_;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LogoutServiceImpl implements LogoutService {
    private final JwtService jwtService;
    private final TokenService tokenService;

    @Transactional
    @Override
    public void logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(BEARER_)) {
            throw new BadRequestBaseException("No token was found in the authorization header.");
        }

        String accessToken = authHeader.substring(BEARER_.length());
        Claims claims = jwtService.extractAllClaims(accessToken);

        if (!jwtService.isTokenHasType(claims, TokenType.ACCESS)) {
            throw new BadRequestBaseException("To logout, you need to use an access token, not an refresh token");
        }

        tokenService.revokeTokenByJti(claims);
        SecurityContextHolder.clearContext();
    }

}
