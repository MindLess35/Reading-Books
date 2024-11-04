package com.senla.readingbooks.dto;

import jakarta.validation.constraints.NotBlank;

public record HtmlContentDto(@NotBlank String htmlContent) {
}
