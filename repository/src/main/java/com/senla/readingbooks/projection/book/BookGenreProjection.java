package com.senla.readingbooks.projection.book;

import com.senla.readingbooks.enums.book.Genre;

public interface BookGenreProjection {
    Long getBookId();

    Genre getGenre();
}
