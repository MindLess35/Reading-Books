package com.senla.readingbooks.dto;

import com.senla.readingbooks.enums.user.Gender;
import com.senla.readingbooks.enums.user.Role;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;
import java.time.LocalDate;

public record UserFilterDto(
        String username,
        String email,
        String status,
        String about,
        @PastOrPresent
        LocalDate birthDate,
        Gender gender,
        Role role,
        Boolean isHasAvatar,
        @Past
        Instant createdAt,
        @Past
        Instant updatedAt,
        @PositiveOrZero
        Long booksWrittenCount) {
}