package com.senla.readingbooks.dto.auth;


import jakarta.validation.constraints.NotBlank;


@NotBlank(message = "{password.notblank}")
public record ResetPasswordDto(String newPassword, String confirmationPassword) {
}
