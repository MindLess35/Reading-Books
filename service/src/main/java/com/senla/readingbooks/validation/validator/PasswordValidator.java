package com.senla.readingbooks.validation.validator;

import com.senla.readingbooks.validation.annotation.Password;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<Password, String> {
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 64;
    private static final Pattern LOWER_CASE = Pattern.compile("[a-zа-я]");
    private static final Pattern UPPER_CASE = Pattern.compile("[A-ZА-Я]");
    private static final Pattern DIGIT = Pattern.compile("\\d");
    private static final Pattern SPECIAL_CHAR = Pattern.compile("[{}()\\[\\]!?@#$%^&*_\\-]");
    private static final Pattern NO_WHITESPACE = Pattern.compile("^\\S*$");

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (!StringUtils.hasText(password)) {
            buildConstraintViolation(context, "Password cannot be blank");
            return false;
        }

        if (password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
            buildConstraintViolation(context, String.format("Password must be between %d and %d characters long", MIN_LENGTH, MAX_LENGTH));
            return false;
        }

        if (!NO_WHITESPACE.matcher(password).matches()) {
            buildConstraintViolation(context, "Password cannot contain spaces");
            return false;
        }
//          закомментировал, чтобы можно было использовать обычный пароль, а не добавлять к нему каждый раз символы
//          при 400 статусе из-за этих проверок

//        if (!LOWER_CASE.matcher(password).find()) {
//            buildConstraintViolation(context, "Password must contain at least one lowercase letter");
//            return false;
//        }
//
//        if (!UPPER_CASE.matcher(password).find()) {
//            buildConstraintViolation(context, "Password must contain at least one uppercase letter");
//            return false;
//        }
//
//        if (!DIGIT.matcher(password).find()) {
//            buildConstraintViolation(context, "Password must contain at least one digit");
//            return false;
//        }
//
//        if (!SPECIAL_CHAR.matcher(password).find()) {
//            buildConstraintViolation(context, "Password must contain at least one " +
//                                              "special character ({}()[]?!@#$%^&*_\\-)");
//            return false;
//        }

        return true;
    }

    private void buildConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}