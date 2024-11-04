package com.senla.readingbooks.dto.bookcollection;

import java.io.Serializable;

public record BookCollectionReadDto(
        Long id,
        String title,
        String description,
        Boolean isPublic,
        Boolean isDraft,
        Long userId) implements Serializable {
}