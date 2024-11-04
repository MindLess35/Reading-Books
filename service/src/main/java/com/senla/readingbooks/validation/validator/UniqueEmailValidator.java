package com.senla.readingbooks.validation.validator;

import com.senla.readingbooks.property.CacheProperty;
import com.senla.readingbooks.repository.jpa.user.UserRepository;
import com.senla.readingbooks.validation.annotation.UniqueEmail;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;

import static com.senla.readingbooks.service.impl.user.UserServiceImpl.CACHE_USER_EMAIL;

@Component
@RequiredArgsConstructor
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {
    private final UserRepository userRepository;
    private final CacheProperty cacheProperty;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (!StringUtils.hasText(email)) {
            buildConstraintViolation(context, "Email cannot be blank");
            return false;
        }

        String keyForEmail = CACHE_USER_EMAIL + email;
        boolean isEmailInCache = Boolean.TRUE.equals(redisTemplate.hasKey(keyForEmail));
        if (isEmailInCache || userRepository.existsByEmail(email)) {
            if (!isEmailInCache) {
                redisTemplate.opsForValue().set(keyForEmail, email, Duration.ofSeconds(cacheProperty.getTtl()));
            }
            buildConstraintViolation(context, "Email is already taken");
            return false;
        }

        return true;
    }

    private void buildConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}

