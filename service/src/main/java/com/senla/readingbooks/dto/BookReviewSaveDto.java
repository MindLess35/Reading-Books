package com.senla.readingbooks.dto;

import com.senla.readingbooks.validation.group.OnCreate;
import com.senla.readingbooks.validation.group.OnUpdate;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Positive;

public record BookReviewSaveDto(
        @Null(groups = OnUpdate.class)
        @NotNull(groups = OnCreate.class)
        @Positive(groups = OnCreate.class)
        Long userId,
        @Null(groups = OnUpdate.class)
        @NotNull(groups = OnCreate.class)
        @Positive(groups = OnCreate.class)
        Long bookId,
        @NotNull
        @Positive
        @Max(10)
        Float rating,
        @NotNull
        Boolean isSpoiler,
        @NotBlank(groups = OnCreate.class)
        String htmlContent) {
}