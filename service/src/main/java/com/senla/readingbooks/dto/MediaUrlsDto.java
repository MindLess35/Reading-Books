package com.senla.readingbooks.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.hibernate.validator.constraints.URL;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.List;

public record MediaUrlsDto(
        @NotEmpty
        @UniqueElements
        List<@URL @NotBlank String> mediaUrls) {
}
