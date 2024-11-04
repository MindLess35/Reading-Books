package com.senla.readingbooks.repository.jpa.book;

import com.senla.readingbooks.entity.book.BookSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BookSeriesRepository extends JpaRepository<BookSeries, Long> {

    @Query(value = """
            SELECT COUNT(bs.id) > 0
            FROM book_series bs
            WHERE bs.id = :seriesId AND bs.user_id = :userId
            LIMIT 1
            """, nativeQuery = true)
    boolean userIsAuthorOfSeries(Long seriesId, Long userId);

    @Query(value = """
            SELECT COUNT(b.id) > 0
            FROM books b
            WHERE b.series_id = :seriesId AND b.status <> :bookStatus
            LIMIT 1
            """, nativeQuery = true)
    boolean existsPublishedBookInSeries(Long seriesId, String bookStatus);

}
