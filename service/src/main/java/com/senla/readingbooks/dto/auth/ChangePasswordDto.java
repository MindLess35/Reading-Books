package com.senla.readingbooks.dto.auth;

import com.senla.readingbooks.validation.annotation.Password;
import io.swagger.v3.oas.annotations.media.Schema;


public record ChangePasswordDto(

        @Password
        @Schema(description = "Current password of the user.")
        String currentPassword,

        @Password
        @Schema(description = "New password. Password must be 8-64 characters long, containing at least" +
                              " one uppercase letter, one lowercase letter, one number, and " +
                              "one special character from (){}[]!@#$%^&*_-.")
        String newPassword,

        @Password
        @Schema(description = "Password confirmation. Must match the new password.")
        String confirmationPassword) {
}
