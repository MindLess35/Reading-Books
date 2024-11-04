package com.senla.readingbooks.service.impl.user;

import com.senla.readingbooks.dto.UserFilterDto;
import com.senla.readingbooks.dto.UsersAsPageDto;
import com.senla.readingbooks.dto.auth.ChangePasswordDto;
import com.senla.readingbooks.dto.user.UserReadDto;
import com.senla.readingbooks.dto.user.UserUpdateDto;
import com.senla.readingbooks.entity.user.User;
import com.senla.readingbooks.enums.user.Role;
import com.senla.readingbooks.event.BookCreatedEvent;
import com.senla.readingbooks.exception.base.ResourceNotFoundException;
import com.senla.readingbooks.exception.password.InvalidPasswordException;
import com.senla.readingbooks.mapper.UserMapper;
import com.senla.readingbooks.property.CacheProperty;
import com.senla.readingbooks.repository.jpa.user.UserRepository;
import com.senla.readingbooks.service.interfaces.auth.TokenService;
import com.senla.readingbooks.service.interfaces.user.ElasticUserService;
import com.senla.readingbooks.service.interfaces.user.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final TokenService tokenService;
    private final CacheProperty cacheProperty;
    private final ElasticUserService elasticUserService;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, User> userRedisTemplate;
    private final RedisTemplate<String, String> stringRedisTemplate;
    @PersistenceContext
    private EntityManager entityManager;
    private static final String USER_NOT_FOUND = "User with id [%d] not found";
    private static final String CACHE_USER_ENTITY = "user::entity::id::";
    public static final String CACHE_USER_EMAIL = "user::email::";
    public static final String CACHE_USER_USERNAME = "user::username::";

    @Override
    public UserReadDto findUserById(Long id) {
        return userMapper.toDto(findById(id));
    }

    @Override
    public User findById(Long id) {
        String keyForUser = CACHE_USER_ENTITY + id;
        User user = userRedisTemplate.opsForValue().get(keyForUser);
        if (user == null) {
            user = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND.formatted(id)));
            userRedisTemplate.opsForValue().set(keyForUser, user, Duration.ofSeconds(cacheProperty.getTtl()));
        }
        return user;
    }

    @Override
    public Page<UsersAsPageDto> findUsersAsPage(Pageable pageable, UserFilterDto filter) {
        return userRepository.findUsersAsPage(pageable, filter);
    }

    @Override
    public List<User> findUsersByIds(List<Long> ids) {
        List<String> keys = ids.stream()
                .map(id -> CACHE_USER_ENTITY + id)
                .toList();

        List<User> cachedUsers = userRedisTemplate.opsForValue().multiGet(keys);
        List<User> foundUsers = new ArrayList<>();
        List<Long> missingIds = new ArrayList<>();
        for (int i = 0; i < ids.size() && cachedUsers != null; i++) {
            User user = cachedUsers.get(i);
            if (user != null) {
                foundUsers.add(user);
            } else {
                missingIds.add(ids.get(i));
            }
        }
        if (missingIds.isEmpty()) {
            return foundUsers;
        }

        List<User> retrievedUsers = userRepository.findUsersByIdIn(missingIds);
        Map<String, User> usersToCache = retrievedUsers.stream()
                .collect(Collectors.toMap(user -> CACHE_USER_ENTITY + user.getId(), Function.identity()));

        userRedisTemplate.opsForValue().multiSet(usersToCache);
        foundUsers.addAll(retrievedUsers);
        return foundUsers;
    }

    @Override
    @Transactional
    public UserReadDto updateUser(Long id, UserUpdateDto dto) {
        User savedUser = userRepository.findById(id)
                .map(u -> userMapper.updateEntity(dto, u))
                .map(userRepository::saveAndFlush)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND.formatted(id)));
        String key = CACHE_USER_ENTITY + id;
        userRedisTemplate.opsForValue().set(key, savedUser, Duration.ofSeconds(cacheProperty.getTtl()));

        String email = savedUser.getEmail();
        if (!email.equals(dto.email())) {
            String keyForEmail = CACHE_USER_EMAIL + email;
            stringRedisTemplate.opsForValue().set(keyForEmail, email, Duration.ofSeconds(cacheProperty.getTtl()));
        }
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND.formatted(id)));
        tokenService.revokeAllUserTokensById(id);
        userRepository.delete(user);
        elasticUserService.deleteById(id);

        String keyForUser = CACHE_USER_ENTITY + id;
        String keyForEmail = CACHE_USER_EMAIL + user.getEmail();
        String keyForUsername = CACHE_USER_USERNAME + user.getUsername();
        stringRedisTemplate.delete(List.of(keyForUsername, keyForEmail, keyForUser));
    }

    @Override
    @Transactional
    public void changePassword(Long id, ChangePasswordDto passwordDto) {
        if (!passwordDto.newPassword().equals(passwordDto.confirmationPassword()))
            throw new InvalidPasswordException("passwordConfirmation", "New password and its confirmation do not match");

        String key = CACHE_USER_ENTITY + id;
        User user = userRedisTemplate.opsForValue().get(key);
        if (user == null) {
            user = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND.formatted(id)));
        }
        if (!passwordEncoder.matches(passwordDto.currentPassword(), user.getPassword()))
            throw new InvalidPasswordException("wrongPassword", "Wrong password");

        user.setPassword(passwordEncoder.encode(passwordDto.newPassword()));
        userRepository.updatePassword(id, user.getPassword());
        userRedisTemplate.opsForValue().set(key, user, Duration.ofSeconds(cacheProperty.getTtl()));
    }

    @EventListener
    public void makeUsersAuthors(BookCreatedEvent event) {
        List<User> authors = event.users();
        authors.removeIf(author -> author.getRole().equals(Role.AUTHOR));
        if (!authors.isEmpty()) {
            userRepository.makeUsersAuthors(authors.stream().map(User::getId).toList(), Role.AUTHOR);
            Map<String, User> usersToCache = authors.stream()
                    .collect(Collectors.toMap(user -> CACHE_USER_ENTITY + user.getId(), Function.identity()));
            usersToCache.values().forEach(author -> entityManager.detach(author));
            usersToCache.values().forEach(author -> author.setRole(Role.AUTHOR));
            userRedisTemplate.opsForValue().multiSet(usersToCache);
        }
    }

}