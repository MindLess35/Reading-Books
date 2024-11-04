package com.senla.readingbooks.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;

public record BookCollectionFilterDto(
        String title,
        Boolean isPublic,
        Boolean isDraft,
        @Max(10)
        @Positive
        Float rating,
        @PositiveOrZero
        Integer ratingsCount,
        @PositiveOrZero
        Integer viewsCount,
        @PositiveOrZero
        Integer likesCount,
        @Past
        Instant updatedAt,
        @Past
        Instant createdAt,
        @PositiveOrZero
        Long booksCount) {
}
