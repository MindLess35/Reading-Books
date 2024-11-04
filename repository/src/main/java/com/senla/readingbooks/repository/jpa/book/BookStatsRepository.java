package com.senla.readingbooks.repository.jpa.book;

import com.senla.readingbooks.entity.book.BookStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface BookStatsRepository extends JpaRepository<BookStatistics, Long> {

    @Modifying
    @Query("""
            UPDATE BookStatistics bs
            SET bs.updatedAt = :now
            WHERE bs.book.id = :bookId
            """)
    void updateStatistics(Long bookId, Instant now);

    @Modifying
    @Query("""
            UPDATE BookStatistics bs
            SET bs.publicationDate = :now
            WHERE bs.book.id = :bookId
            """)
    void updatePublicationDate(Long bookId, Instant now);
}
