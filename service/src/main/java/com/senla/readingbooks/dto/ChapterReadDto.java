package com.senla.readingbooks.dto;

import com.senla.readingbooks.enums.book.AccessType;

import java.io.Serializable;
import java.time.Instant;

public record ChapterReadDto(
        Long id,
        Long bookId,
        String title,
        AccessType accessType,
        Instant publicationDate,
        Boolean isDraft,
        String contentUrl) implements Serializable {
}
