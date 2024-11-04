package com.senla.readingbooks.dto;

public record UserLibraryAsPageDto(
        Long bookId,
        String bookTitle,
        String coverUrl,
        Long authorId,
        String authorUsername) {
}
