package com.senla.readingbooks.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.senla.readingbooks.enums.user.Role;
import com.senla.readingbooks.enums.user.TokenType;
import com.senla.readingbooks.exception.response.ResponseErrorBody;
import com.senla.readingbooks.exception.security.AuthException;
import com.senla.readingbooks.property.CacheProperty;
import com.senla.readingbooks.service.interfaces.auth.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

import static com.senla.readingbooks.service.interfaces.auth.JwtService.BEARER_;
import static com.senla.readingbooks.service.interfaces.auth.JwtService.ROLE;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final ObjectMapper objectMapper;
    private final JwtService jwtService;
    private final CacheProperty cacheProperty;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER_)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtService.extractAllClaims(authHeader.substring(BEARER_.length()));
            Double tokenExpiration = redisTemplate.opsForZSet().score(cacheProperty.getJtiBlackListName(), jwtService.extractJti(claims).toString());
            if (tokenExpiration != null) {
                throw new AuthException("Token is blacklisted and cannot be used");
            }

            if (jwtService.isTokenExpired(claims) || jwtService.isTokenHasType(claims, TokenType.REFRESH)) {
                filterChain.doFilter(request, response);
                return;
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                var authToken = new UsernamePasswordAuthenticationToken(
                        claims.getSubject(),
                        null,
                        Collections.singleton(new SimpleGrantedAuthority(claims.get(ROLE, Role.class).name())));

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        } catch (JwtException | AuthException ex) {
            response.setStatus(UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            ResponseErrorBody errorBody = ResponseErrorBody.builder()
                    .status(UNAUTHORIZED.value())
                    .cause(UNAUTHORIZED.getReasonPhrase())
                    .exception(ex.getClass().getName())
                    .message(ex.getMessage())
                    .path(request.getRequestURI())
                    .build();
            response.getWriter().write(objectMapper.writeValueAsString(errorBody));
            return;
        }
        filterChain.doFilter(request, response);
    }
}