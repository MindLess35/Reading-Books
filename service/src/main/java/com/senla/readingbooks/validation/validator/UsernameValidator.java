package com.senla.readingbooks.validation.validator;

import com.senla.readingbooks.validation.annotation.Username;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

public class UsernameValidator implements ConstraintValidator<Username, String> {
    private static final String USERNAME_PATTERN = "^[A-Za-zА-Яа-я0-9(){}\\[\\]!@#$%^&*_-]{4,32}$";

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        if (!StringUtils.hasText(username)) {
            buildConstraintViolation(context, "Username cannot be blank");
            return false;
        }

        if (!username.matches(USERNAME_PATTERN)) {
            buildConstraintViolation(context, "Username must be between 4 and 32 characters," +
                                              " contain only letters, numbers, special " +
                                              "characters ([](){}!@#$%^&*_-), and cannot contain spaces.");
            return false;
        }

        return true;
    }

    private void buildConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}


