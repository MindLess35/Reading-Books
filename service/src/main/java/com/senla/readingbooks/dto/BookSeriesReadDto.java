package com.senla.readingbooks.dto;

import com.senla.readingbooks.enums.book.PublicationStatus;

import java.io.Serializable;
import java.time.Instant;

public record BookSeriesReadDto(
        Long id,
        String title,
        String description,
        Instant createdAt,
        Instant updatedAt,
        PublicationStatus status,
        Long userId,
        String username) implements Serializable {
}
