package com.senla.readingbooks.repository.jpa.book;

import com.senla.readingbooks.entity.book.BookReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BookReviewRepository extends JpaRepository<BookReview, Long> {
    @Query("""
            SELECT br
            FROM BookReview br
            WHERE br.book.id = :bookId AND br.user.id = :userId
            """)
    Optional<BookReview> findByUserIdAndBookId(Long userId, Long bookId);

    @Query(value = """
            SELECT COUNT(br.id) > 0
            FROM book_reviews br
            WHERE br.id = :reviewId AND br.user_id = :userId
            LIMIT 1
            """, nativeQuery = true)
    boolean userIsAuthorOfReview(Long reviewId, Long userId);

}
