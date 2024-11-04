package com.senla.readingbooks.dto.user;

import com.senla.readingbooks.enums.book.LibrarySection;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UserLibraryAddDto(
        @NotNull
        @Positive
        Long userId,
        @NotNull
        @Positive
        Long bookId,
        @NotNull
        LibrarySection section) {
}