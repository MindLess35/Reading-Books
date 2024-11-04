package com.senla.readingbooks.repository.jpa.book;

import com.senla.readingbooks.entity.collection.BookCollectionStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface BookCollectionStatsRepository extends JpaRepository<BookCollectionStats, Long> {
    @Modifying
    @Query("""
                UPDATE BookCollectionStats bcs
                SET bcs.updatedAt = :now
                WHERE bcs.bookCollection.id = :bookCollectionId
            """)
    void updateStatistics(Long bookCollectionId, Instant now);
}
