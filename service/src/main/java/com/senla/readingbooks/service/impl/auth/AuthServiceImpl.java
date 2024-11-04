package com.senla.readingbooks.service.impl.auth;

import com.senla.readingbooks.dto.auth.JwtResponseDto;
import com.senla.readingbooks.dto.user.UserCreateDto;
import com.senla.readingbooks.dto.user.UserLoginDto;
import com.senla.readingbooks.entity.user.User;
import com.senla.readingbooks.enums.user.TokenType;
import com.senla.readingbooks.exception.base.BadRequestBaseException;
import com.senla.readingbooks.exception.security.AuthException;
import com.senla.readingbooks.mapper.UserMapper;
import com.senla.readingbooks.repository.jpa.user.UserRepository;
import com.senla.readingbooks.service.interfaces.auth.AuthService;
import com.senla.readingbooks.service.interfaces.auth.JwtService;
import com.senla.readingbooks.service.interfaces.auth.TokenService;
import com.senla.readingbooks.service.interfaces.user.ElasticUserService;
import com.senla.readingbooks.util.AuthUtil;
import com.senla.readingbooks.util.UserHolder;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

import static com.senla.readingbooks.service.interfaces.auth.JwtService.BEARER_;
import static org.apache.http.HttpHeaders.AUTHORIZATION;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {
    private final JwtService jwtService;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final ElasticUserService elasticUserService;

    @Override
    @Transactional
    public JwtResponseDto createUser(UserCreateDto dto, String deviceId) {
        validateHeader(X_DEVICE_ID, deviceId);
        checkUserAlreadyAuthenticated();
        User user = userMapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);

        UUID jti = UUID.randomUUID();
        String accessToken = jwtService.generateAccessToken(savedUser, jti);
        String refreshToken = jwtService.generateRefreshToken(savedUser.getId(), jti, deviceId);

        tokenService.saveToken(savedUser, jti, deviceId);
        elasticUserService.saveUserDocument(savedUser);
        return new JwtResponseDto(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public JwtResponseDto login(UserLoginDto dto, String deviceId) {
        validateHeader(X_DEVICE_ID, deviceId);
        checkUserAlreadyAuthenticated();
        User user = Optional.of(new UsernamePasswordAuthenticationToken(dto.username(), dto.password()))
                .map(authenticationManager::authenticate)
                .map(Authentication::getPrincipal)
                .map(UserHolder.class::cast)
                .map(UserHolder::user)
                .orElseThrow(() -> new BadCredentialsException(
                        "The user with the username [%s] not found in the application or the password is incorrect.".formatted(dto.username())));

        UUID jti = UUID.randomUUID();
        Long userId = user.getId();
        String accessToken = jwtService.generateAccessToken(user, jti);
        String refreshToken = jwtService.generateRefreshToken(userId, jti, deviceId);

        tokenService.revokeTokenByUserIdAndDeviceIdSoft(userId, deviceId);
        tokenService.saveToken(user, jti, deviceId);
        return new JwtResponseDto(accessToken, refreshToken);
    }

    @Override
    @Transactional(noRollbackFor = AuthException.class)
    public JwtResponseDto refreshToken(String authHeader, String deviceId) {
        validateHeader(X_DEVICE_ID, deviceId);
        checkUserAlreadyAuthenticated();
        String refreshToken = getTokenFromHeader(authHeader);
        Claims claims = jwtService.extractAllClaims(refreshToken);

        if (!jwtService.isTokenHasType(claims, TokenType.REFRESH)) {
            throw new JwtException("To refresh a token, you need to use an refresh token, not an access token");
        }

        Long userId = jwtService.extractSubject(claims);
        User user = userRepository.findById(userId).orElseThrow(() -> new AuthException(
                "User by id [%d] extracted from the refresh token is not found.".formatted(userId)));

        tokenService.revokeTokenByJti(claims, userId, deviceId);

        UUID newJti = UUID.randomUUID();
        String newAccessToken = jwtService.generateAccessToken(user, newJti);
        String newRefreshToken = jwtService.generateRefreshToken(userId, newJti, deviceId);

        tokenService.saveToken(user, newJti, deviceId);
        return new JwtResponseDto(newAccessToken, newRefreshToken);
    }

    @Override
    @Transactional
    public void revokeToken(String authHeader, String deviceId) {
        validateHeader(X_DEVICE_ID, deviceId);
        String accessToken = getTokenFromHeader(authHeader);
        Claims claims = jwtService.extractAllClaims(accessToken);

        if (!jwtService.isTokenHasType(claims, TokenType.ACCESS)) {
            throw new JwtException("To revoke a token, you need to use an access token, not an refresh token");
        }
        Long userId = jwtService.extractSubject(claims);
        tokenService.revokeTokenByUserIdAndDeviceId(userId, deviceId);
    }

    private static String getTokenFromHeader(String authHeader) {
        validateHeader(authHeader, AUTHORIZATION);
        if (!authHeader.startsWith(BEARER_)) {
            throw new BadRequestBaseException("No token was found in the authorization header.");
        }
        return authHeader.substring(BEARER_.length());
    }


    private static void validateHeader(String name, String value) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestBaseException("Header %s must not be null nor blank".formatted(name));
        }
    }

    private static void checkUserAlreadyAuthenticated() {
        if (AuthUtil.isUserAlreadyAuthenticated())
            throw new BadRequestBaseException("User is already authenticated and cannot create a new account, login to the application or refresh token");
    }
}

