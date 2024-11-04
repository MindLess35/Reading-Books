package com.senla.readingbooks.repository.jpa.book;

import com.senla.readingbooks.entity.book.Chapter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    @Query("""
            SELECT c
            FROM Chapter c
            WHERE c.book.id = :bookId
            """)
    Page<Chapter> findAllByBookId(Long bookId, Pageable pageable);

    @Query(value = """
            SELECT COUNT(c.id) > 0
            FROM book_authors ba
            JOIN books b ON ba.book_id = b.id
            JOIN chapters c ON c.book_id = b.id
            WHERE ba.author_id = :userId AND c.id = :chapterId
            """, nativeQuery = true)
    boolean userIsAuthorOfChapter(Long chapterId, Long userId);
}
