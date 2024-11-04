package com.senla.readingbooks.repository.jdbc;

import com.senla.readingbooks.enums.book.Genre;

import java.util.List;

public interface JdbcBookRepository {
    void saveTagsInBatch(Long bookId, List<String> tags);

    void saveGenresInBatch(Long bookId, List<Genre> genres);

    void saveBookAuthorsInBatch(Long bookId, List<Long> authorIds);

}