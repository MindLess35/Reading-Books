package com.senla.readingbooks.dto.comment;

import com.senla.readingbooks.enums.EntityType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record EntityIdAndTypeDto(
        @NotNull
        @Positive
        Long entityId,
        @NotNull
        EntityType entityType) {
}
