package com.senla.readingbooks.repository.jdbc;

import com.senla.readingbooks.enums.book.Genre;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

@RequiredArgsConstructor
public class JdbcBookRepositoryImpl implements JdbcBookRepository {
    private final JdbcTemplate jdbcTemplate;
    private static final String BOOK_AUTHORS_INSERT = "INSERT INTO book_authors (book_id, author_id) VALUES (?, ?)";
    private static final String BOOK_GENRES_INSERT = "INSERT INTO book_genres (book_id, genre) VALUES (?, ?)";
    private static final String BOOK_TAGS_INSERT = "INSERT INTO book_tags (book_id, tag) VALUES (?, ?)";

    public void saveBookAuthorsInBatch(Long bookId, List<Long> authorIds) {
        if (authorIds.size() == 1) {
            jdbcTemplate.update(BOOK_AUTHORS_INSERT, bookId, authorIds.iterator().next());

        } else {
            jdbcTemplate.batchUpdate(BOOK_AUTHORS_INSERT, authorIds, authorIds.size(), (ps, authorId) -> {
                ps.setLong(1, bookId);
                ps.setLong(2, authorId);
            });
        }
    }

    public void saveGenresInBatch(Long bookId, List<Genre> genres) {
        if (genres.size() == 1) {
            jdbcTemplate.update(BOOK_GENRES_INSERT, bookId, genres.iterator().next().name());

        } else {
            jdbcTemplate.batchUpdate(BOOK_GENRES_INSERT, genres, genres.size(), (ps, genre) -> {
                ps.setLong(1, bookId);
                ps.setString(2, genre.name());
            });
        }
    }

    public void saveTagsInBatch(Long bookId, List<String> tags) {
        if (tags.isEmpty()) {
            return;

        } else if (tags.size() == 1) {
            jdbcTemplate.update(BOOK_TAGS_INSERT, bookId, tags.iterator().next());

        } else {
            jdbcTemplate.batchUpdate(BOOK_TAGS_INSERT, tags, tags.size(), (ps, tag) -> {
                ps.setLong(1, bookId);
                ps.setString(2, tag);
            });
        }
    }
}



