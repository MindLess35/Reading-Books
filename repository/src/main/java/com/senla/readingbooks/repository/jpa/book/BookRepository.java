package com.senla.readingbooks.repository.jpa.book;

import com.senla.readingbooks.entity.book.Book;
import com.senla.readingbooks.projection.EntityIdWithImageUrlProjection;
import com.senla.readingbooks.projection.book.BookAuthorProjection;
import com.senla.readingbooks.projection.book.BookGenreProjection;
import com.senla.readingbooks.repository.criteria.CriteriaBookRepository;
import com.senla.readingbooks.repository.jdbc.JdbcBookRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends
        JpaRepository<Book, Long>,
        CriteriaBookRepository,
        JdbcBookRepository {
    /*
        В этом методе будет декартово произведение строк максимум на 90 строк из 1.
     1 (исходная книга) * 3 (от авторов) * 3 (от жанров) * 10 (от тегов) = 90 (джоин на бук сериес не расплитывает строки,
     тк каждой книге может быть сопоставлена максимум только одна серия, в которой книга состоит), но по факту у книг в
     абсолютном большенстве случаев будет только один автор, да и тегов с жанрами может быть меньше максимума, поэтому
     чаще будет в итоге меньше 30 строк выгружаться в память приложения, что уже не так уж и много. Для такой реализации
     пришлось сделать жанры и теги сетом, а не листом в сущности книга, но, учитывая, что это стринг и енам, а не сущности,
     то не надо париться на счёт иквелз и хешкод методов.
         Альтернатива - отдельные запросы для вытаскивания каждой коллекции. Тут уже надо тестировать, что лучше:
     вытащить всё одним сложным запросом или отдельными, нагружая сеть и БД. Решил оставить так.
    */

    //    @EntityGraph(attributePaths = {"bookSeries", "users", "genres", "tags"})
    @Query("""
            SELECT b
            FROM Book b
            LEFT JOIN FETCH b.bookSeries
            JOIN FETCH b.users
            JOIN FETCH b.genres
            LEFT JOIN FETCH b.tags
            WHERE b.id = :id
            """)
    Optional<Book> findBookByIdEager(Long id);

    @Query(value = """
                SELECT bg.book_id   AS bookId,
                       bg.genre     AS genre
                FROM book_genres bg
                WHERE bg.book_id IN :bookIds
            """, nativeQuery = true)
    List<BookGenreProjection> findGenresByBookIds(List<Long> bookIds);

    @Query(value = """
                SELECT ba.book_id      AS bookId,
                       u.id            AS authorId,
                       u.username      AS username,
                       u.avatar_url    AS avatarUrl
                FROM book_authors ba
                JOIN users u ON ba.author_id = u.id
                WHERE ba.book_id IN :bookIds
            """, nativeQuery = true)
    List<BookAuthorProjection> findAuthorsByBookIds(List<Long> bookIds);


    @Modifying
    @Query("""
            UPDATE Book b
            SET b.coverUrl = :coverUrl
            WHERE b.id = :bookId
            """)
    void updateCoverById(String coverUrl, Long bookId);

    @Modifying
    @Query("""
            UPDATE Book b
            SET b.coverUrl = null
            WHERE b.id = :bookId
            """)
    void updateCoverToNullById(Long bookId);

    @Query("""
            SELECT b.coverUrl AS imageUrl,
                   b.id AS entityId
            FROM Book b
            WHERE b.id = :bookId
            """)
    Optional<EntityIdWithImageUrlProjection> findCoverWithBookIdById(Long bookId);

    @Query("""
            SELECT b.id AS bookId,
                   b.title AS title,
                   b.coverUrl AS coverUrl,
                   u.id AS authorId,
                   u.username AS username
            FROM Book b
            JOIN b.users u
            WHERE b.id IN :bookIds
            """)
    List<BookAuthorProjection> findBooksWithAuthorsByIds(List<Long> bookIds);

    @Query(value = """
            SELECT COUNT(1) > 0
            FROM book_authors
            WHERE book_id = :bookId AND author_id = :userId
            """, nativeQuery = true)
    boolean userIsAuthorOfBook(Long bookId, Long userId);

    @Query(value = """
                SELECT COUNT(c.id) > 0
                FROM chapters c
                WHERE c.book_id = :bookId AND c.is_draft = false
                LIMIT 1
            """, nativeQuery = true)
    boolean existsPublishedChapterById(Long bookId);

}
