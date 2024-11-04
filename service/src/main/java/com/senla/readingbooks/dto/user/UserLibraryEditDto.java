package com.senla.readingbooks.dto.user;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


public record UserLibraryEditDto(
        @NotNull
        @Positive
        Long userId,
        @NotNull
        @Positive
        Long bookId) {
}
