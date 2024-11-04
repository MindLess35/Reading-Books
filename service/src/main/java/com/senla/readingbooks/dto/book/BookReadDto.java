package com.senla.readingbooks.dto.book;

import com.senla.readingbooks.dto.AuthorWithAvatarDto;
import com.senla.readingbooks.enums.book.AccessType;
import com.senla.readingbooks.enums.book.BookForm;
import com.senla.readingbooks.enums.book.Genre;
import com.senla.readingbooks.enums.book.PublicationStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public final class BookReadDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -1180948995681337608L;

    private final Long id;
    private final String title;
    private final BookForm form;
    private final AccessType accessType;
    private final PublicationStatus status;
    private final BigDecimal price;
    private final String annotation;
    private final String authorNote;
    private final String coverUrl;
    private final Long seriesId;
    private final String seriesTitle;
    @Setter
    private Set<Genre> genres;
    @Setter
    private Set<String> tags;
    @Setter
    private Set<AuthorWithAvatarDto> authors;

}
