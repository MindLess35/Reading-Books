package com.senla.readingbooks.dto;

import java.time.Instant;

public record BookCollectionsAsPageDto(
        Long id,
        String title,
        String description,
        Boolean isPublic,
        Boolean isDraft,
        Integer likesCount,
        Integer dislikesCount,
        Float rating,
        Integer ratingsCount,
        Integer viewsCount,
        Instant createdAt,
        Instant updatedAt,
        Long booksCount,
        Long authorId,
        String authorUsername) {
}
