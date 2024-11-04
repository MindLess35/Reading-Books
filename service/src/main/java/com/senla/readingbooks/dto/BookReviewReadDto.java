package com.senla.readingbooks.dto;

import java.io.Serializable;
import java.time.Instant;

public record BookReviewReadDto(
        Long id,
        Long bookId,
        Long userId,
        Float rating,
        Integer viewsCount,
        Integer likesCount,
        Integer dislikesCount,
        Boolean isSpoiler,
        Instant createdAt,
        Instant updatedAt,
        String contentUrl) implements Serializable {
}