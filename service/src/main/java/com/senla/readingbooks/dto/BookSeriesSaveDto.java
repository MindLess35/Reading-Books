package com.senla.readingbooks.dto;

import com.senla.readingbooks.validation.group.OnCreate;
import com.senla.readingbooks.validation.group.OnUpdate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record BookSeriesSaveDto(
        @NotBlank
        @Size(max = 64)
        String title,

        @Size(max = 255)
        String description,
        @Null(groups = OnUpdate.class)
        @NotNull(groups = OnCreate.class)
        @Positive(groups = OnCreate.class)
        Long userId) {
}