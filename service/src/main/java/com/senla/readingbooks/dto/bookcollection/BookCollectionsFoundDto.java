package com.senla.readingbooks.dto.bookcollection;

public record BookCollectionsFoundDto(
        Long id,
        String title,
        Integer booksCount,
        Long authorId,
        String username,
        String avatarUrl) {
}
