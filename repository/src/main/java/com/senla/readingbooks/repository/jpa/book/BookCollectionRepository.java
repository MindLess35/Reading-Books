package com.senla.readingbooks.repository.jpa.book;

import com.senla.readingbooks.entity.collection.BookCollection;
import com.senla.readingbooks.projection.book.BookCollectionProjection;
import com.senla.readingbooks.repository.criteria.CriteriaBookCollectionRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookCollectionRepository extends JpaRepository<BookCollection, Long>, CriteriaBookCollectionRepository {

    @Query(value = """
            SELECT DISTINCT
                   bc.id AS collectionId,
                   bc.title AS title,
                   bc.description AS description,
                   u.id AS authorId,
                   u.username AS username,
                   u.avatar_url AS avatarUrl,
                   COUNT(bci.book_id) OVER (PARTITION BY bc.id) AS booksCount
            FROM book_collections bc
            JOIN users u ON bc.user_id = u.id
            LEFT JOIN book_collection_items bci ON bc.id = bci.collection_id
            WHERE bc.id IN (:collectionIds)
            """, nativeQuery = true)
    List<BookCollectionProjection> findCollectionsWithAuthorsAndBooksCount(List<Long> collectionIds);

    @Query(value = """
            SELECT COUNT(bc.id) > 0
            FROM book_collections bc
            WHERE bc.id = :collectionId AND bc.user_id = :userId
            LIMIT 1
            """, nativeQuery = true)
    boolean userIsAuthorOfCollection(Long collectionId, Long userId);


}
