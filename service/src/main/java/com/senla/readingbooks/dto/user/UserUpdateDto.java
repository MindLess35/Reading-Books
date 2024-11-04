package com.senla.readingbooks.dto.user;

import com.senla.readingbooks.enums.user.Gender;
import com.senla.readingbooks.validation.annotation.UniqueEmail;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UserUpdateDto(
        @Size(max = 64)
        String status,
        @Size(max = 512)
        String about,
        @PastOrPresent
        LocalDate birthDate,
        Gender gender,
        @Size(max = 255)
        @UniqueEmail
        @Email
        String email) {
}
