package com.senla.readingbooks.service.impl.user;

import com.senla.readingbooks.dto.UserFilterDto;
import com.senla.readingbooks.dto.UsersAsPageDto;
import com.senla.readingbooks.dto.auth.ChangePasswordDto;
import com.senla.readingbooks.dto.user.UserReadDto;
import com.senla.readingbooks.dto.user.UserUpdateDto;
import com.senla.readingbooks.entity.user.User;
import com.senla.readingbooks.enums.user.Gender;
import com.senla.readingbooks.enums.user.Role;
import com.senla.readingbooks.exception.base.ResourceNotFoundException;
import com.senla.readingbooks.exception.password.InvalidPasswordException;
import com.senla.readingbooks.mapper.UserMapper;
import com.senla.readingbooks.property.CacheProperty;
import com.senla.readingbooks.repository.jpa.user.UserRepository;
import com.senla.readingbooks.service.interfaces.auth.TokenService;
import com.senla.readingbooks.service.interfaces.user.ElasticUserService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private TokenService tokenService;
    @Mock
    private CacheProperty cacheProperty;
    @Mock
    private ElasticUserService elasticUserService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RedisTemplate<String, User> userRedisTemplate;
    @Mock
    private RedisTemplate<String, String> stringRedisTemplate;
    @Mock
    private ValueOperations<String, User> userValueOperations;
    @Mock
    private ValueOperations<String, String> stringValueOperations;
    @Mock
    private EntityManager entityManager;
    @InjectMocks
    private UserServiceImpl userService;
    private static final Long USER_ID = 1L;
    private static final String USER_KEY = "user::entity::id::" + USER_ID;
    private static final String CACHE_USER_ENTITY = "user::entity::id::";
    private static final String CURRENT_PASSWORD = "currentPassword";
    private static final String NEW_PASSWORD = "NewPassword1!";

    @BeforeEach
    void setUp() {
        when(userRedisTemplate.opsForValue()).thenReturn(userValueOperations);
        when(stringRedisTemplate.opsForValue()).thenReturn(stringValueOperations);
    }

    private User getUserWithId() {
        return User.builder()
                .id(USER_ID)
                .username("username")
                .email("email")
                .status("status")
                .role(Role.READER)
                .gender(Gender.M)
                .build();
    }


    private UserReadDto getUserReadDto() {
        return new UserReadDto(
                USER_ID,
                "username",
                "email",
                "status",
                "about",
                null,
                Gender.M,
                Role.READER,
                Instant.now(),
                null
        );
    }

    @Test
    void findUserById_ShouldReturnUserDto_WhenUserExistsInCache() {
        User cachedUser = getUserWithId();
        UserReadDto expectedDto = getUserReadDto();

        when(userValueOperations.get(USER_KEY)).thenReturn(cachedUser);
        when(userMapper.toDto(cachedUser)).thenReturn(expectedDto);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(cachedUser));

        UserReadDto actualResult = userService.findUserById(USER_ID);

        assertEquals(expectedDto, actualResult);
        verify(userMapper).toDto(cachedUser);
        verifyNoInteractions(tokenService, elasticUserService, passwordEncoder, entityManager);
    }

    @Test
    void findUserById_ShouldFetchFromRepositoryAndCache_WhenUserNotInCache() {
        User userFromDb = getUserWithId();
        UserReadDto expectedDto = getUserReadDto();

        when(userValueOperations.get(USER_KEY)).thenReturn(null);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(userFromDb));
        when(userMapper.toDto(userFromDb)).thenReturn(expectedDto);
        when(cacheProperty.getTtl()).thenReturn(600);

        UserReadDto actualResult = userService.findUserById(USER_ID);

        assertEquals(expectedDto, actualResult);
        verify(userRepository).findById(USER_ID);
        verify(userMapper).toDto(userFromDb);
        verifyNoInteractions(tokenService, elasticUserService, passwordEncoder, entityManager);
    }

    @Test
    void findUserById_ShouldThrowResourceNotFoundException_WhenUserDoesNotExist() {
        when(userValueOperations.get(USER_KEY)).thenReturn(null);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUserById(USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User with id [" + USER_ID + "] not found");

        verify(userRepository).findById(USER_ID);
        verifyNoInteractions(userMapper, cacheProperty, tokenService, elasticUserService, passwordEncoder, entityManager);
    }

    @Test
    void findById_ShouldFetchFromRepositoryAndCache_WhenUserNotInCache() {
        User userFromDb = getUserWithId();

        when(userValueOperations.get(USER_KEY)).thenReturn(null);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(userFromDb));
        when(cacheProperty.getTtl()).thenReturn(600);

        User actualResult = userService.findById(USER_ID);

        assertEquals(userFromDb, actualResult);
        verify(userRepository).findById(USER_ID);
        verifyNoInteractions(userMapper, tokenService, elasticUserService, passwordEncoder, entityManager);
    }

    @Test
    void findById_ShouldThrowResourceNotFoundException_WhenUserDoesNotExist() {
        when(userValueOperations.get(USER_KEY)).thenReturn(null);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User with id [" + USER_ID + "] not found");

        verify(userRepository).findById(USER_ID);
        verifyNoInteractions(userMapper, cacheProperty, tokenService, elasticUserService, passwordEncoder, entityManager);
    }

    @Test
    void findUsersAsPage_ShouldReturnPagedResult() {
        Pageable pageable = Pageable.ofSize(10);
        UserFilterDto filter = new UserFilterDto(
                "username",
                "email",
                "status",
                "about",
                LocalDate.now(),
                Gender.M,
                Role.READER,
                true,
                Instant.now().minusSeconds(60),
                Instant.now().minusSeconds(30),
                5L
        );
        UsersAsPageDto usersAsPageDto = new UsersAsPageDto(
                USER_ID,
                "username",
                "email",
                "status",
                "about",
                Gender.M,
                Instant.now(),
                Instant.now(),
                LocalDate.now(),
                Role.READER,
                "avatarUrl",
                5L
        );

        Page<UsersAsPageDto> expectedPage = new PageImpl<>(Collections.singletonList(usersAsPageDto));

        when(userRepository.findUsersAsPage(pageable, filter)).thenReturn(expectedPage);

        Page<UsersAsPageDto> actualPage = userService.findUsersAsPage(pageable, filter);

        assertEquals(actualPage, expectedPage);
        verify(userRepository).findUsersAsPage(pageable, filter);
        verifyNoInteractions(userRedisTemplate);
    }


    @Test
    void updateUser_ShouldUpdateAndCacheUser_WhenUserExists() {
        User existingUser = getUserWithId();
        UserUpdateDto updateDto = getUserUpdateDto();
        User updatedUser = User.builder()
                .id(USER_ID)
                .username(existingUser.getUsername())
                .email(updateDto.email())
                .status(updateDto.status())
                .about(updateDto.about())
                .birthDate(updateDto.birthDate())
                .gender(updateDto.gender())
                .role(Role.READER)
                .build();
        UserReadDto expectedDto = getUserReadDto();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
        when(userMapper.updateEntity(updateDto, existingUser)).thenReturn(updatedUser);
        when(userRepository.saveAndFlush(updatedUser)).thenReturn(updatedUser);
        when(userMapper.toDto(updatedUser)).thenReturn(expectedDto);

        UserReadDto actualDto = userService.updateUser(USER_ID, updateDto);

        assertEquals(actualDto, expectedDto);
        verify(userRepository).findById(USER_ID);
        verify(userMapper).updateEntity(updateDto, existingUser);
        verify(userRepository).saveAndFlush(updatedUser);
        verify(userMapper).toDto(updatedUser);
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    private static UserUpdateDto getUserUpdateDto() {
        return new UserUpdateDto("updatedStatus", "updatedAbout", LocalDate.of(2000, 1, 1), Gender.F, "updatedEmail");
    }

    @Test
    void updateUser_ShouldThrowException_WhenUserDoesNotExist() {
        UserUpdateDto updateDto = getUserUpdateDto();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(USER_ID, updateDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User with id [%d] not found".formatted(USER_ID));

        verify(userRepository).findById(USER_ID);
        verifyNoInteractions(userMapper, userRedisTemplate, stringRedisTemplate);
    }

    @Test
    void deleteUser_ShouldDeleteUserAndClearCache_WhenUserExists() {
        User user = getUserWithId();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        userService.deleteUser(USER_ID);

        verify(userRepository).findById(USER_ID);
        verify(userRepository).delete(user);
        verify(tokenService).revokeAllUserTokensById(USER_ID);
        verify(elasticUserService).deleteById(USER_ID);
        verifyNoMoreInteractions(userRepository, tokenService, elasticUserService);
    }

    @Test
    void deleteUser_ShouldThrowResourceNotFoundException_WhenUserDoesNotExist() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User with id [%d] not found".formatted(USER_ID));

        verify(userRepository).findById(USER_ID);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(tokenService, elasticUserService, stringRedisTemplate);
    }

    @Test
    void changePassword_ShouldThrowInvalidPasswordException_WhenPasswordConfirmationDoesNotMatch() {
        ChangePasswordDto passwordDto = new ChangePasswordDto(CURRENT_PASSWORD, NEW_PASSWORD, "differentPassword");

        assertThatThrownBy(() -> userService.changePassword(USER_ID, passwordDto))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessage("New password and its confirmation do not match")
                .extracting("invalidFieldName").isEqualTo("passwordConfirmation");

        verifyNoInteractions(userRepository, userValueOperations, passwordEncoder);
    }

    @Test
    void changePassword_ShouldThrowResourceNotFoundException_WhenUserDoesNotExistInCacheOrRepository() {
        ChangePasswordDto passwordDto = new ChangePasswordDto(CURRENT_PASSWORD, NEW_PASSWORD, NEW_PASSWORD);
        String key = CACHE_USER_ENTITY + USER_ID;

        when(userRedisTemplate.opsForValue()).thenReturn(userValueOperations);
        when(userValueOperations.get(key)).thenReturn(null);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.changePassword(USER_ID, passwordDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User with id [%d] not found".formatted(USER_ID));

        verify(userRepository).findById(USER_ID);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }


}
