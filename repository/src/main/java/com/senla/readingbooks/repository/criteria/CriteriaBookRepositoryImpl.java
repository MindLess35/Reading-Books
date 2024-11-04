package com.senla.readingbooks.repository.criteria;

import com.senla.readingbooks.dto.BookFilterDto;
import com.senla.readingbooks.dto.BooksAsPageDto;
import com.senla.readingbooks.entity.AuditingEntityBase_;
import com.senla.readingbooks.entity.book.Book;
import com.senla.readingbooks.entity.book.BookSeries;
import com.senla.readingbooks.entity.book.BookSeries_;
import com.senla.readingbooks.entity.book.BookStatistics;
import com.senla.readingbooks.entity.book.BookStatistics_;
import com.senla.readingbooks.entity.book.Book_;
import com.senla.readingbooks.enums.book.Genre;
import com.senla.readingbooks.exception.NoSuchOrderByFieldException;
import com.senla.readingbooks.util.PredicateBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
public class CriteriaBookRepositoryImpl implements CriteriaBookRepository {
    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public Page<BooksAsPageDto> findBooksAsPage(Pageable pageable, BookFilterDto filter) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<BooksAsPageDto> query = cb.createQuery(BooksAsPageDto.class);

        Root<Book> book = query.from(Book.class);
        Join<Book, BookStatistics> bookStats = book.join(Book_.bookStatistics);
        Order order = extractOrder(pageable, cb, bookStats);

        Long total = countTotalResult(cb, filter);
        if (total == 0) {
            return Page.empty(pageable);
        }

        Join<Book, BookSeries> bookSeries = book.join(Book_.bookSeries, JoinType.LEFT);
        Join<Book, Genre> genre = null;

        if (filter.genre() != null) {
            genre = book.join(Book_.genres);
        }

        Subquery<Long> subquery = null;
        Root<Book> subBook = null;
        Join<Book, Genre> subGenre = null;

        if (filter.excludedGenres() != null) {
            subquery = query.subquery(Long.class);
            subBook = subquery.from(Book.class);
            subGenre = subBook.join(Book_.genres);
        }

        Predicate predicate = createPredicate(filter, cb, book, bookStats, bookSeries, genre, subquery, subBook, subGenre);
        constructQuery(query, cb, book, bookSeries, bookStats, predicate, order);

        List<BooksAsPageDto> resultList = executeQueryWithPagination(pageable, query);
        return new PageImpl<>(resultList, pageable, total);
    }

    private Long countTotalResult(CriteriaBuilder cb, BookFilterDto filter) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Book> book = countQuery.from(Book.class);

        Join<Book, BookStatistics> bookStats = book.join(Book_.bookStatistics);
        Join<Book, BookSeries> bookSeries = book.join(Book_.bookSeries, JoinType.LEFT);
        Join<Book, Genre> genre = null;

        if (filter.genre() != null || filter.excludedGenres() != null) {
            genre = book.join(Book_.genres);
        }

        Subquery<Long> subquery = null;
        Root<Book> subBook = null;
        Join<Book, Genre> subGenre = null;

        if (filter.excludedGenres() != null) {
            subquery = countQuery.subquery(Long.class);
            subBook = subquery.from(Book.class);
            subGenre = subBook.join(Book_.genres);
        }
        Predicate predicate = createPredicate(filter, cb, book, bookStats, bookSeries, genre, subquery, subBook, subGenre);

        countQuery
                .select(cb.countDistinct(book.get(Book_.id)))
                .where(predicate);
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private static Predicate createPredicate(BookFilterDto filter,
                                             CriteriaBuilder cb,
                                             Root<Book> book,
                                             Join<Book, BookStatistics> bookStats,
                                             Join<Book, BookSeries> bookSeries,
                                             Join<Book, Genre> genre,
                                             Subquery<Long> subquery,
                                             Root<Book> subBook,
                                             Join<Book, Genre> subGenre) {
        return PredicateBuilder.builder()
                .add(filter.title(), title -> cb.like(book.get(Book_.title), "%" + title + "%"))
                .add(filter.form(), form -> cb.equal(book.get(Book_.form), form))
                .add(filter.price(), price -> cb.lessThanOrEqualTo(book.get(Book_.price), price))
                .add(filter.accessType(), accessType -> cb.equal(book.get(Book_.accessType), accessType))
                .add(filter.bookStatus(), bookStatus -> cb.equal(book.get(Book_.status), bookStatus))
                .add(filter.genre(), g -> cb.equal(genre, g))
                .add(filter.excludedGenres(), excludedGenres -> cb.not(book.get(Book_.id)
                        .in(subquery.select(subBook.get(Book_.id)).where(subGenre.in(excludedGenres)))))

                .add(filter.inSeries(), inSeries -> Boolean.TRUE.equals(inSeries)
                        ? cb.isNotNull(book.get(Book_.bookSeries))
                        : cb.isNull(book.get(Book_.bookSeries)))

                .add(filter.isHasCover(), isHasCover -> isHasCover
                        ? cb.isNotNull(book.get(Book_.coverUrl))
                        : cb.isNull(book.get(Book_.coverUrl)))


                .add(filter.rating(), rating -> cb.greaterThanOrEqualTo(bookStats
                        .get(BookStatistics_.rating), rating))

                .add(filter.charactersCount(), charactersCount -> cb.greaterThanOrEqualTo(bookStats
                        .get(BookStatistics_.charactersCount), charactersCount))

                .add(filter.pagesCount(), pagesCount -> cb.greaterThanOrEqualTo(bookStats
                        .get(BookStatistics_.pagesCount), pagesCount))

                .add(filter.ratingsCount(), ratingsCount -> cb.greaterThanOrEqualTo(bookStats
                        .get(BookStatistics_.ratingsCount), ratingsCount))

                .add(filter.viewsCount(), viewsCount -> cb.greaterThanOrEqualTo(bookStats
                        .get(BookStatistics_.viewsCount), viewsCount))

                .add(filter.likesCount(), likesCount -> cb.greaterThanOrEqualTo(bookStats
                        .get(BookStatistics_.likesCount), likesCount))

                .add(filter.libraryAddCount(), libraryAddCount -> cb.greaterThanOrEqualTo(bookStats
                        .get(BookStatistics_.libraryAddCount), libraryAddCount))

                .add(filter.createdAt(), createdAt -> cb.greaterThanOrEqualTo(bookStats
                        .get(AuditingEntityBase_.createdAt), createdAt))

                .add(filter.updatedAt(), updatedAt -> cb.greaterThanOrEqualTo(bookStats
                        .get(AuditingEntityBase_.updatedAt), updatedAt))

                .add(filter.publicationDate(), publicationDate -> cb.greaterThanOrEqualTo(bookStats
                        .get(BookStatistics_.publicationDate), publicationDate))

                .add(filter.daysSincePublished(), includeSinceDate -> cb.greaterThanOrEqualTo(bookStats
                        .get(BookStatistics_.publicationDate), includeSinceDate.getStartDate()))

                .add(filter.daysSinceUpdated(), includeSinceDate -> cb.greaterThanOrEqualTo(bookStats
                        .get(AuditingEntityBase_.updatedAt), includeSinceDate.getStartDate()))


                .add(filter.seriesStatus(), seriesStatus -> cb.equal(bookSeries
                        .get(BookSeries_.status), seriesStatus))
                .buildAnd(cb);
    }

    private static void constructQuery(CriteriaQuery<BooksAsPageDto> query,
                                       CriteriaBuilder cb,
                                       Root<Book> book,
                                       Join<Book, BookSeries> bookSeries,
                                       Join<Book, BookStatistics> bookStats,
                                       Predicate predicate,
                                       Order order) {
        query.select(cb.construct(
                        BooksAsPageDto.class,
                        book.get(Book_.id),
                        book.get(Book_.title),
                        book.get(Book_.form),
                        book.get(Book_.accessType),
                        book.get(Book_.status),
                        book.get(Book_.price),
                        book.get(Book_.annotation),
                        book.get(Book_.coverUrl),
                        bookSeries.get(BookSeries_.id).alias("seriesId"),
                        bookSeries.get(BookSeries_.title).alias("seriesTitle"),
                        bookSeries.get(BookSeries_.status).alias("seriesStatus"),
                        bookStats.get(BookStatistics_.rating),
                        bookStats.get(BookStatistics_.ratingsCount),
                        bookStats.get(BookStatistics_.pagesCount),
                        bookStats.get(BookStatistics_.charactersCount),
                        bookStats.get(BookStatistics_.viewsCount),
                        bookStats.get(AuditingEntityBase_.updatedAt),
                        bookStats.get(AuditingEntityBase_.createdAt),
                        bookStats.get(BookStatistics_.publicationDate),
                        bookStats.get(BookStatistics_.likesCount),
                        bookStats.get(BookStatistics_.libraryAddCount)))
                .where(predicate)
                .orderBy(order);
    }

    private List<BooksAsPageDto> executeQueryWithPagination(Pageable pageable, CriteriaQuery<BooksAsPageDto> query) {
        return entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }

    private static Order extractOrder(Pageable pageable, CriteriaBuilder cb, Path<BookStatistics> bookStats) {
        Sort.Order sortOrder = pageable.getSort().get().findFirst().orElseThrow();
        String property = sortOrder.getProperty().replaceAll("[\\[\\]\"]", "");

        return switch (property) {
            case BookStatistics_.VIEWS_COUNT -> sortOrder.isAscending()
                    ? cb.asc(bookStats.get(BookStatistics_.VIEWS_COUNT))
                    : cb.desc(bookStats.get(BookStatistics_.VIEWS_COUNT));
            case BookStatistics_.LIKES_COUNT -> sortOrder.isAscending()
                    ? cb.asc(bookStats.get(BookStatistics_.LIKES_COUNT))
                    : cb.desc(bookStats.get(BookStatistics_.LIKES_COUNT));
            case BookStatistics_.PAGES_COUNT -> sortOrder.isAscending()
                    ? cb.asc(bookStats.get(BookStatistics_.PAGES_COUNT))
                    : cb.desc(bookStats.get(BookStatistics_.PAGES_COUNT));
            case BookStatistics_.LIBRARY_ADD_COUNT -> sortOrder.isAscending()
                    ? cb.asc(bookStats.get(BookStatistics_.LIBRARY_ADD_COUNT))
                    : cb.desc(bookStats.get(BookStatistics_.LIBRARY_ADD_COUNT));
            case BookStatistics_.PUBLICATION_DATE -> sortOrder.isAscending()
                    ? cb.asc(cb.coalesce(bookStats.get(BookStatistics_.PUBLICATION_DATE), Instant.MAX))
                    : cb.desc(cb.coalesce(bookStats.get(BookStatistics_.PUBLICATION_DATE), Instant.MIN));
            case BookStatistics_.RATING -> sortOrder.isAscending()
                    ? cb.asc(cb.coalesce(bookStats.get(BookStatistics_.RATING), Integer.MAX_VALUE))
                    : cb.desc(cb.coalesce(bookStats.get(BookStatistics_.RATING), Integer.MIN_VALUE));
            default -> throw new NoSuchOrderByFieldException("Invalid sort property: " + property);
        };
    }


}
