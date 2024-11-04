package com.senla.readingbooks.validation.validator;

import com.senla.readingbooks.property.CacheProperty;
import com.senla.readingbooks.repository.jpa.user.UserRepository;
import com.senla.readingbooks.validation.annotation.UniqueUsername;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;

import static com.senla.readingbooks.service.impl.user.UserServiceImpl.CACHE_USER_USERNAME;

@Component
@RequiredArgsConstructor
public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String> {
    private final UserRepository userRepository;
    private final CacheProperty cacheProperty;
    private final RedisTemplate<String, String> redisTemplate;
    private static final String USERNAME_PATTERN = "^[A-Za-zА-Яа-я0-9!()\\[\\]{}@#?$%^&*_-]{4,32}$";

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        if (!StringUtils.hasText(username)) {
            buildConstraintViolation(context, "Username cannot be blank");
            return false;
        }

        if (!username.matches(USERNAME_PATTERN)) {
            buildConstraintViolation(context, "Username must be between 4 and 32 characters," +
                                              " contain only letters, numbers, special " +
                                              "characters (()[]{}@#?$%^&*_-), and cannot contain spaces.");
            return false;
        }

        String keyForUsername = CACHE_USER_USERNAME + username;
        boolean isUsernameInCache = Boolean.TRUE.equals(redisTemplate.hasKey(keyForUsername));
        if (isUsernameInCache || userRepository.existsByUsername(username)) {
            if (!isUsernameInCache) {
                redisTemplate.opsForValue().set(keyForUsername, username, Duration.ofSeconds(cacheProperty.getTtl()));
            }
            buildConstraintViolation(context, "Username is already taken");
            return false;
        }
        return true;
    }

    private void buildConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}
