package com.senla.readingbooks.repository.jpa.book;

import com.senla.readingbooks.entity.book.ChapterStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface ChapterStatsRepository extends JpaRepository<ChapterStatistics, Long> {

    @Modifying
    @Query("""
                UPDATE ChapterStatistics cs
                SET cs.updatedAt = :now
                WHERE cs.chapter.id = :chapterId
            """)
    void updateStatistics(Long chapterId, Instant now);

}
