package com.senla.readingbooks.repository.jpa.book;

import com.senla.readingbooks.entity.collection.BookCollectionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BookCollectionItemRepository extends JpaRepository<BookCollectionItem, Long> {
    boolean existsByBookCollectionIdAndBookId(Long collectionId, Long bookId);

    @Query(value = """
            SELECT COUNT(bci.id) > 0
            FROM book_collection_items bci
            JOIN books b ON bci.book_id = b.id
            WHERE bci.collection_id = :collectionId AND b.status <> :bookStatus
            LIMIT 1
            """, nativeQuery = true)
    boolean existsPublishedBookInCollection(Long collectionId, String bookStatus);
}

