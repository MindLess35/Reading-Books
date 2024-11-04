package com.senla.readingbooks.dto.user;

import com.senla.readingbooks.enums.user.Gender;
import com.senla.readingbooks.enums.user.Role;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

public record UserReadDto(
        Long id,
        String username,
        String email,
        String status,
        String about,
        LocalDate birthDate,
        Gender gender,
        Role role,
        Instant createdAt,
        Instant updatedAt) implements Serializable {

}
