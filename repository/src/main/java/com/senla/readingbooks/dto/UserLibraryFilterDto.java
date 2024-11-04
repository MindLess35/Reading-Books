package com.senla.readingbooks.dto;

import com.senla.readingbooks.enums.book.BookForm;
import com.senla.readingbooks.enums.book.Genre;
import com.senla.readingbooks.enums.book.LibrarySection;
import com.senla.readingbooks.enums.book.PublicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UserLibraryFilterDto(
        @NotNull
        @Positive
        Long userId,
        BookForm form,
        Genre genre,
        PublicationStatus status,
        LibrarySection section) {
}
