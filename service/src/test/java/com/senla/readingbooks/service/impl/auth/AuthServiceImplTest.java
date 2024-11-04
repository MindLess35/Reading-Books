package com.senla.readingbooks.service.impl.auth;

import com.senla.readingbooks.dto.auth.JwtResponseDto;
import com.senla.readingbooks.dto.user.UserCreateDto;
import com.senla.readingbooks.dto.user.UserLoginDto;
import com.senla.readingbooks.entity.user.User;
import com.senla.readingbooks.enums.user.Role;
import com.senla.readingbooks.enums.user.TokenType;
import com.senla.readingbooks.exception.base.BadRequestBaseException;
import com.senla.readingbooks.exception.security.AuthException;
import com.senla.readingbooks.mapper.UserMapper;
import com.senla.readingbooks.repository.jpa.user.UserRepository;
import com.senla.readingbooks.service.interfaces.auth.JwtService;
import com.senla.readingbooks.service.interfaces.auth.TokenService;
import com.senla.readingbooks.service.interfaces.user.ElasticUserService;
import com.senla.readingbooks.util.AuthUtil;
import com.senla.readingbooks.util.UserHolder;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private TokenService tokenService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ElasticUserService elasticUserService;
    @InjectMocks
    private AuthServiceImpl authService;
    private static final Long USER_ID = 1L;
    private static final String DEVICE_ID = "deviceId";

    @Test
    void createUser_Success() {
        try (MockedStatic<AuthUtil> authUtilMockedStatic = mockStatic(AuthUtil.class);
             MockedStatic<UUID> uuidMockedStatic = mockStatic(UUID.class)) {

            authUtilMockedStatic.when(AuthUtil::isUserAlreadyAuthenticated).thenReturn(false);

            UUID mockedUUID = UUID.randomUUID();
            uuidMockedStatic.when(UUID::randomUUID).thenReturn(mockedUUID);

            UserCreateDto dto = getUserCreateDto();
            User user = User.builder()
                    .username("username")
                    .email("email")
                    .password("password")
                    .role(Role.READER)
                    .build();

            User savedUser = getUserWithId();

            String accessToken = "accessToken";
            String refreshToken = "refreshToken";

            when(userMapper.toEntity(dto)).thenReturn(user);
            when(passwordEncoder.encode(dto.password())).thenReturn("encodedPassword");
            when(userRepository.save(user)).thenReturn(savedUser);
            when(jwtService.generateAccessToken(savedUser, mockedUUID)).thenReturn(accessToken);
            when(jwtService.generateRefreshToken(savedUser.getId(), mockedUUID, DEVICE_ID)).thenReturn(refreshToken);

            JwtResponseDto actualResult = authService.createUser(dto, DEVICE_ID);

            assertEquals(actualResult.accessToken(), accessToken);
            assertEquals(actualResult.refreshToken(), refreshToken);

            verify(userMapper).toEntity(dto);
            verify(passwordEncoder).encode(dto.password());
            verify(userRepository).save(user);
            verify(jwtService).generateAccessToken(savedUser, mockedUUID);
            verify(jwtService).generateRefreshToken(savedUser.getId(), mockedUUID, DEVICE_ID);
            verify(tokenService).saveToken(savedUser, mockedUUID, DEVICE_ID);
            verify(elasticUserService).saveUserDocument(savedUser);
        }
    }

    private static User getUserWithId() {
        return User.builder()
                .id(USER_ID)
                .username("username")
                .email("email")
                .password("encodedPassword")
                .role(Role.READER)
                .build();
    }

    private static UserCreateDto getUserCreateDto() {
        return new UserCreateDto("username", "email", "password");
    }

    @Test
    void createUser_ShouldThrowBadRequestBaseException_WhenUserAlreadyAuthenticated() {
        try (MockedStatic<AuthUtil> authUtilMockedStatic = mockStatic(AuthUtil.class)) {
            authUtilMockedStatic.when(AuthUtil::isUserAlreadyAuthenticated)
                    .thenThrow(new BadRequestBaseException("User is already authenticated"));

            assertThatThrownBy(() -> authService.createUser(getUserCreateDto(), DEVICE_ID))
                    .isInstanceOf(BadRequestBaseException.class)
                    .hasMessage("User is already authenticated");
            verifyNoInteractions(userMapper, passwordEncoder, userRepository, jwtService, tokenService, elasticUserService);
        }
    }

    @Test
    void login_Success() {
        try (MockedStatic<AuthUtil> authUtilMockedStatic = mockStatic(AuthUtil.class);
             MockedStatic<UUID> uuidMockedStatic = mockStatic(UUID.class)) {

            authUtilMockedStatic.when(AuthUtil::isUserAlreadyAuthenticated).thenReturn(false);

            UUID mockedUUID = UUID.randomUUID();
            uuidMockedStatic.when(UUID::randomUUID).thenReturn(mockedUUID);

            UserLoginDto loginDto = new UserLoginDto("username", "password");
            User user = getUserWithId();

            String accessToken = "accessToken";
            String refreshToken = "refreshToken";

            Authentication authToken = new UsernamePasswordAuthenticationToken(loginDto.username(), loginDto.password());
            UserHolder userHolder = new UserHolder(user);
            Authentication mockedAuthentication = mock(Authentication.class);

            when(authenticationManager.authenticate(authToken)).thenReturn(mockedAuthentication);
            when(mockedAuthentication.getPrincipal()).thenReturn(userHolder);
            when(jwtService.generateAccessToken(user, mockedUUID)).thenReturn(accessToken);
            when(jwtService.generateRefreshToken(user.getId(), mockedUUID, DEVICE_ID)).thenReturn(refreshToken);

            JwtResponseDto actualResult = authService.login(loginDto, DEVICE_ID);

            assertEquals(actualResult.accessToken(), accessToken);
            assertEquals(actualResult.refreshToken(), refreshToken);

            verify(authenticationManager).authenticate(authToken);
            verify(jwtService).generateAccessToken(user, mockedUUID);
            verify(jwtService).generateRefreshToken(user.getId(), mockedUUID, DEVICE_ID);
            verify(tokenService).saveToken(user, mockedUUID, DEVICE_ID);
        }
    }

    @Test
    void login_ShouldThrowBadRequestBaseException_WhenUserAlreadyAuthenticated() {
        try (MockedStatic<AuthUtil> authUtilMockedStatic = mockStatic(AuthUtil.class)) {
            authUtilMockedStatic.when(AuthUtil::isUserAlreadyAuthenticated)
                    .thenReturn(true);

            UserLoginDto loginDto = new UserLoginDto("username", "password");

            assertThatThrownBy(() -> authService.login(loginDto, DEVICE_ID))
                    .isInstanceOf(BadRequestBaseException.class)
                    .hasMessage("User is already authenticated and cannot create a new account, login to the application or refresh token");

            verifyNoInteractions(authenticationManager, jwtService, tokenService);
        }
    }

    @Test
    void login_ShouldThrowBadCredentialsException_WhenInvalidCredentials() {
        try (MockedStatic<AuthUtil> authUtilMockedStatic = mockStatic(AuthUtil.class)) {
            authUtilMockedStatic.when(AuthUtil::isUserAlreadyAuthenticated).thenReturn(false);

            UserLoginDto loginDto = new UserLoginDto("username", "password");
            String username = loginDto.username();
            Authentication auth = new UsernamePasswordAuthenticationToken(username, loginDto.password());

            when(authenticationManager.authenticate(auth)).thenThrow(new BadCredentialsException("The user with the username [%s] not found in the application or the password is incorrect.".formatted(username)));

            assertThatThrownBy(() -> authService.login(loginDto, DEVICE_ID))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessage("The user with the username [%s] not found in the application or the password is incorrect.".formatted(username));

            verify(authenticationManager).authenticate(auth);
            verifyNoInteractions(jwtService, tokenService);
        }
    }


    @Test
    void refreshToken_Success() {
        String authHeader = "Bearer validRefreshToken";
        String refreshToken = "validRefreshToken";
        User user = getUserWithId();
        String accessToken = "newAccessToken";
        String newRefreshToken = "newRefreshToken";

        try (MockedStatic<UUID> uuidMockedStatic = mockStatic(UUID.class)) {
            UUID newJti = UUID.randomUUID();
            uuidMockedStatic.when(UUID::randomUUID).thenReturn(newJti);
            Claims claims = mock(Claims.class);

            when(jwtService.extractAllClaims(refreshToken)).thenReturn(claims);
            when(jwtService.isTokenHasType(claims, TokenType.REFRESH)).thenReturn(true);
            when(jwtService.extractSubject(claims)).thenReturn(USER_ID);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(jwtService.generateAccessToken(user, newJti)).thenReturn(accessToken);
            when(jwtService.generateRefreshToken(USER_ID, newJti, DEVICE_ID)).thenReturn(newRefreshToken);

            JwtResponseDto actualResult = authService.refreshToken(authHeader, DEVICE_ID);

            assertEquals(actualResult.accessToken(), accessToken);
            assertEquals(actualResult.refreshToken(), newRefreshToken);

            verify(jwtService).extractAllClaims(refreshToken);
            verify(jwtService).isTokenHasType(claims, TokenType.REFRESH);
            verify(jwtService).extractSubject(claims);
            verify(userRepository).findById(USER_ID);
            verify(tokenService).revokeTokenByJti(claims, USER_ID, DEVICE_ID);
            verify(jwtService).generateAccessToken(user, newJti);
            verify(jwtService).generateRefreshToken(USER_ID, newJti, DEVICE_ID);
            verify(tokenService).saveToken(user, newJti, DEVICE_ID);
            verifyNoMoreInteractions(jwtService, userRepository, tokenService);
        }
    }

    @Test
    void refreshToken_ShouldThrowJwtException_WhenTokenNotRefreshType() {
        String authHeader = "Bearer invalidRefreshToken";
        String refreshToken = "invalidRefreshToken";
        Claims claims = mock(Claims.class);

        when(jwtService.extractAllClaims(refreshToken)).thenReturn(claims);
        when(jwtService.isTokenHasType(claims, TokenType.REFRESH)).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken(authHeader, DEVICE_ID))
                .isInstanceOf(JwtException.class)
                .hasMessage("To refresh a token, you need to use an refresh token, not an access token");

        verify(jwtService).extractAllClaims(refreshToken);
        verify(jwtService).isTokenHasType(claims, TokenType.REFRESH);
        verifyNoMoreInteractions(jwtService);
        verifyNoInteractions(userRepository, tokenService);
    }

    @Test
    void refreshToken_ShouldThrowAuthException_WhenUserNotFound() {
        String authHeader = "Bearer validRefreshToken";
        String refreshToken = "validRefreshToken";
        Claims claims = mock(Claims.class);

        when(jwtService.extractAllClaims(refreshToken)).thenReturn(claims);
        when(jwtService.isTokenHasType(claims, TokenType.REFRESH)).thenReturn(true);
        when(jwtService.extractSubject(claims)).thenReturn(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshToken(authHeader, DEVICE_ID))
                .isInstanceOf(AuthException.class)
                .hasMessage("User by id [1] extracted from the refresh token is not found.");

        verify(jwtService).extractAllClaims(refreshToken);
        verify(jwtService).isTokenHasType(claims, TokenType.REFRESH);
        verify(jwtService).extractSubject(claims);
        verify(userRepository).findById(USER_ID);
        verifyNoMoreInteractions(jwtService, userRepository);
        verifyNoInteractions(tokenService);
    }


    @Test
    void revokeToken_Success() {
        String authHeader = "Bearer validAccessToken";
        String accessToken = "validAccessToken";
        Claims claims = mock(Claims.class);

        when(jwtService.extractAllClaims(accessToken)).thenReturn(claims);
        when(jwtService.isTokenHasType(claims, TokenType.ACCESS)).thenReturn(true);
        when(jwtService.extractSubject(claims)).thenReturn(USER_ID);

        authService.revokeToken(authHeader, DEVICE_ID);

        verify(jwtService).extractAllClaims(accessToken);
        verify(jwtService).isTokenHasType(claims, TokenType.ACCESS);
        verify(jwtService).extractSubject(claims);
        verify(tokenService).revokeTokenByUserIdAndDeviceId(USER_ID, DEVICE_ID);
        verifyNoMoreInteractions(jwtService, tokenService);
    }

    @Test
    void revokeToken_ShouldThrowJwtException_WhenTokenNotAccessType() {
        String authHeader = "Bearer invalidAccessToken";
        String accessToken = "invalidAccessToken";
        Claims claims = mock(Claims.class);

        when(jwtService.extractAllClaims(accessToken)).thenReturn(claims);
        when(jwtService.isTokenHasType(claims, TokenType.ACCESS)).thenReturn(false);

        assertThatThrownBy(() -> authService.revokeToken(authHeader, DEVICE_ID))
                .isInstanceOf(JwtException.class)
                .hasMessage("To revoke a token, you need to use an access token, not an refresh token");

        verify(jwtService).extractAllClaims(accessToken);
        verify(jwtService).isTokenHasType(claims, TokenType.ACCESS);
        verifyNoMoreInteractions(jwtService);
        verifyNoInteractions(tokenService);
    }

    @Test
    void revokeToken_ShouldThrowBadRequestBaseException_WhenAuthHeaderInvalid() {
        String invalidAuthHeader = "InvalidTokenHeader";

        assertThatThrownBy(() -> authService.revokeToken(invalidAuthHeader, DEVICE_ID))
                .isInstanceOf(BadRequestBaseException.class)
                .hasMessage("No token was found in the authorization header.");

        verifyNoInteractions(jwtService, tokenService);
    }

}

