package com.senla.readingbooks.dto.comment;

import com.senla.readingbooks.enums.EntityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CommentSaveDto(
        @NotNull
        @Positive
        Long entityId,
        @NotNull
        EntityType entityType,
        @NotNull
        @Positive
        Long userId,
        @Positive
        Long parentId,
        @NotBlank
        String htmlContent) {
}