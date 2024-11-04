package com.senla.readingbooks.service.impl.auth;

import com.senla.readingbooks.entity.user.RefreshToken;
import com.senla.readingbooks.entity.user.User;
import com.senla.readingbooks.enums.user.Role;
import com.senla.readingbooks.exception.base.ResourceNotFoundException;
import com.senla.readingbooks.property.CacheProperty;
import com.senla.readingbooks.property.JwtProperty;
import com.senla.readingbooks.repository.jpa.user.TokenRepository;
import com.senla.readingbooks.service.interfaces.auth.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TokenServiceImplTest {
    @Mock
    private TokenRepository tokenRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ZSetOperations<String, String> zSetOperations;
    @Mock
    private EntityManager entityManager;
    @Mock
    private JwtProperty jwtProperty;
    @Mock
    private CacheProperty cacheProperty;
    @Spy
    @InjectMocks
    private TokenServiceImpl tokenService;

    private static final String DEVICE_ID = "deviceId";
    private static final Long USER_ID = 1L;
    private static final UUID JTI = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        when(jwtProperty.getAccessExpiration()).thenReturn(900000L);
        when(jwtProperty.getRefreshExpiration()).thenReturn(604800000L);
        when(cacheProperty.getJtiBlackListName()).thenReturn("blacklist::jti");
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
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

    @Test
    void saveToken_ShouldPersistToken() {
        tokenService.saveToken(getUserWithId(), JTI, DEVICE_ID);

        verify(entityManager).persist(any(RefreshToken.class));
    }

    @Test
    void addJtiToRedisBlackList_ShouldAddSingleJtiToRedis() {
        tokenService.addJtiToRedisBlackList(List.of(JTI));

        verify(zSetOperations).addIfAbsent(eq(cacheProperty.getJtiBlackListName()), any());
    }

    @Test
    void revokeTokenByJti_ShouldRevokeToken_WhenTokenFound() {
        Claims claims = mock(Claims.class);
        when(jwtService.extractJti(claims)).thenReturn(JTI);
        when(tokenRepository.revokeTokenByJti(JTI)).thenReturn(1);

        tokenService.revokeTokenByJti(claims);

        verify(tokenRepository).revokeTokenByJti(JTI);
        verify(zSetOperations).addIfAbsent(eq(cacheProperty.getJtiBlackListName()), any());
    }

    @Test
    void revokeTokenByJti_ShouldThrowResourceNotFoundException_WhenTokenNotFound() {
        Claims claims = mock(Claims.class);
        when(jwtService.extractJti(claims)).thenReturn(JTI);

        assertThatThrownBy(() -> tokenService.revokeTokenByJti(claims, USER_ID, DEVICE_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("A user with id %d does not have token on a %s device or is it a token for another device"
                        .formatted(USER_ID, DEVICE_ID));

        verify(jwtService).extractJti(claims);
        verify(tokenRepository).findByJtiAndDeviceId(jwtService.extractJti(claims), DEVICE_ID);
        verifyNoInteractions(zSetOperations);
        verifyNoMoreInteractions(tokenRepository);
    }

    @Test
    void revokeTokenByUserIdAndDevice_ShouldRevokeTokenAndAddToRedis_WhenTokenExists() {
        when(tokenRepository.revokeTokenByUserIdAndDeviceId(USER_ID, DEVICE_ID)).thenReturn(JTI);

        tokenService.revokeTokenByUserIdAndDeviceId(USER_ID, DEVICE_ID);

        verify(tokenRepository).revokeTokenByUserIdAndDeviceId(USER_ID, DEVICE_ID);
    }

    @Test
    void revokeAllUserTokensById_ShouldRevokeAllTokensAndAddToRedis() {
        List<UUID> userTokens = List.of(UUID.randomUUID(), UUID.randomUUID());
        when(tokenRepository.revokeAllUserTokens(USER_ID)).thenReturn(userTokens);

        tokenService.revokeAllUserTokensById(USER_ID);

        verify(tokenRepository).revokeAllUserTokens(USER_ID);
        verify(redisTemplate).opsForZSet();
        verify(zSetOperations).addIfAbsent(eq(cacheProperty.getJtiBlackListName()), any());
    }

    @Test
    void revokeTokenByUserIdAndDevice_ShouldThrowException_WhenTokenNotFound() {
        when(tokenRepository.revokeTokenByUserIdAndDeviceId(USER_ID, DEVICE_ID)).thenReturn(null);

        assertThatThrownBy(() -> tokenService.revokeTokenByUserIdAndDeviceId(USER_ID, DEVICE_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("A user with id %d does not have tokens on a %s device".formatted(USER_ID, DEVICE_ID));

        verify(tokenRepository).revokeTokenByUserIdAndDeviceId(USER_ID, DEVICE_ID);
        verifyNoInteractions(zSetOperations);
    }

    @Test
    void addJtiToRedisBlackList_ShouldAddMultipleJtisWithPipeline_WhenMoreThanOneJti() {
        tokenService.addJtiToRedisBlackList(List.of(UUID.randomUUID(), UUID.randomUUID()));

        verify(zSetOperations).addIfAbsent(eq(cacheProperty.getJtiBlackListName()), any());
    }
}

