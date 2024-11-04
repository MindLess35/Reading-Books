package com.senla.readingbooks.dto;

import com.senla.readingbooks.enums.user.Gender;
import com.senla.readingbooks.enums.user.Role;

import java.time.Instant;
import java.time.LocalDate;


public record UsersAsPageDto(
        Long id,
        String username,
        String email,
        String status,
        String about,
        Gender gender,
        Instant createdAt,
        Instant updatedAt,
        LocalDate birthDate,
        Role role,
        String avatarUrl,
        Long booksWrittenCount) {
}
