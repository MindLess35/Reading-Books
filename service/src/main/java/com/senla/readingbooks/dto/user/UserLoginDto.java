package com.senla.readingbooks.dto.user;

import com.senla.readingbooks.validation.annotation.Password;
import com.senla.readingbooks.validation.annotation.Username;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserLoginDto(

        @Username
        @Schema(description = "Username must be between 4 and 32 characters," +
                              " contain only letters, numbers, special " +
                              "characters (!@#$%^&*_-), and cannot contain spaces.")
        String username,

        @Password
        @Schema(description = "Password must be 8-64 characters long, containing at least" +
                              " one uppercase letter, one lowercase letter, one number, " +
                              "one special character from (!@#$%^&*_-.), and cannot contain spaces.")
        String password) {
}
