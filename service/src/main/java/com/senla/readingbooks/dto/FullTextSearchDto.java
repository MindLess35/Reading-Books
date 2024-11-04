package com.senla.readingbooks.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record FullTextSearchDto(
        @Size(max = 255)
        @NotBlank
        String query,
        @NotNull
        @PositiveOrZero
        Integer pageNumber,
        @NotNull
        @Positive
        Short pageSize) {
}