package com.senla.readingbooks.dto;

import com.senla.readingbooks.enums.book.TimeInterval;
import com.senla.readingbooks.enums.book.AccessType;
import com.senla.readingbooks.enums.book.BookForm;
import com.senla.readingbooks.enums.book.Genre;
import com.senla.readingbooks.enums.book.PublicationStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.UniqueElements;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record BookFilterDto(
        String title,
        BookForm form,
        @Max(10)
        @Positive
        Float rating,
        @PositiveOrZero
        Integer ratingsCount,
        @PositiveOrZero
        Integer charactersCount,
        @PositiveOrZero
        Float pagesCount,
        @PositiveOrZero
        Integer viewsCount,
        @PositiveOrZero
        Integer likesCount,
        @PositiveOrZero
        Integer libraryAddCount,
        @PositiveOrZero
        BigDecimal price,
        AccessType accessType,
        PublicationStatus bookStatus,
        PublicationStatus seriesStatus,
        Boolean inSeries,
        TimeInterval daysSincePublished,
        TimeInterval daysSinceUpdated,
        @Past
        Instant updatedAt,
        @Past
        Instant createdAt,
        @Past
        Instant publicationDate,
        Boolean isHasCover,
        Genre genre,
        @UniqueElements
        @Size(max = 10)
        List<@NotNull Genre> excludedGenres) {
}
