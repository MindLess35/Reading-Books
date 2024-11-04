package com.senla.readingbooks.dto;

import com.senla.readingbooks.enums.book.AccessType;
import com.senla.readingbooks.enums.book.BookForm;
import com.senla.readingbooks.enums.book.Genre;
import com.senla.readingbooks.enums.book.PublicationStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@RequiredArgsConstructor
public final class BooksAsPageDto {
    private final Long id;
    private final String title;
    private final BookForm form;
    private final AccessType accessType;
    private final PublicationStatus status;
    private final BigDecimal price;
    private final String annotation;
    private final String coverUrl;
    private final Long seriesId;
    private final String seriesTitle;
    private final PublicationStatus seriesStatus;
    private final Float rating;
    private final Integer ratingsCount;
    private final Float pagesCount;
    private final Integer charactersCount;
    private final Integer viewsCount;
    private final Instant updatedAt;
    private final Instant createdAt;
    private final Instant publicationDate;
    private final Integer likesCount;
    private final Integer libraryAddCount;
    @Setter
    private List<Genre> genres;
    @Setter
    private List<AuthorWithAvatarDto> authors;
}
