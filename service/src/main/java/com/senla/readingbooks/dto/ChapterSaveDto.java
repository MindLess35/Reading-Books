package com.senla.readingbooks.dto;

import com.senla.readingbooks.validation.group.OnCreate;
import com.senla.readingbooks.validation.group.OnUpdate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ChapterSaveDto(
        @Null(groups = OnUpdate.class)
        @NotNull(groups = OnCreate.class)
        @Positive(groups = OnCreate.class)
        Long bookId,
        @NotBlank
        @Size(max = 64)
        String title,
        @NotBlank(groups = OnCreate.class)
        String htmlContent) {
}
