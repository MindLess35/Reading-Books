package com.senla.readingbooks.repository.criteria;

import com.senla.readingbooks.dto.BookCollectionFilterDto;
import com.senla.readingbooks.dto.BookCollectionsAsPageDto;
import com.senla.readingbooks.entity.AuditingEntityBase_;
import com.senla.readingbooks.entity.collection.BookCollection;
import com.senla.readingbooks.entity.collection.BookCollectionItem;
import com.senla.readingbooks.entity.collection.BookCollectionItem_;
import com.senla.readingbooks.entity.collection.BookCollectionStats;
import com.senla.readingbooks.entity.collection.BookCollectionStats_;
import com.senla.readingbooks.entity.collection.BookCollection_;
import com.senla.readingbooks.entity.user.User;
import com.senla.readingbooks.entity.user.User_;
import com.senla.readingbooks.exception.NoSuchOrderByFieldException;
import com.senla.readingbooks.util.PredicateBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
public class CriteriaBookCollectionRepositoryImpl implements CriteriaBookCollectionRepository {
    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public Page<BookCollectionsAsPageDto> findBookCollectionsAsPage(Pageable pageable, BookCollectionFilterDto filter) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<BookCollectionsAsPageDto> query = cb.createQuery(BookCollectionsAsPageDto.class);

        Root<BookCollection> collection = query.from(BookCollection.class);
        Join<BookCollection, User> user = collection.join(BookCollection_.user);
        Join<BookCollection, BookCollectionStats> collectionStats = collection.join(BookCollection_.bookCollectionStats);
        Order order = extractOrder(pageable, cb, collectionStats);

        Long total = countTotalCollections(cb, filter);
        if (total == 0) {
            return Page.empty(pageable);
        }

        Subquery<Long> booksCountSubquery = query.subquery(Long.class);
        Root<BookCollectionItem> bookCollectionItem = booksCountSubquery.from(BookCollectionItem.class);
        booksCountSubquery.select(cb.count(bookCollectionItem))
                .where(cb.equal(bookCollectionItem.get(BookCollectionItem_.bookCollection), collection));

        Predicate predicate = createPredicate(filter, cb, collection, collectionStats, booksCountSubquery);
        constructQuery(query, cb, collection, user, collectionStats, predicate, order, booksCountSubquery);

        List<BookCollectionsAsPageDto> resultList = executeQueryWithPagination(pageable, query);
        return new PageImpl<>(resultList, pageable, total);
    }

    private static void constructQuery(CriteriaQuery<BookCollectionsAsPageDto> query,
                                       CriteriaBuilder cb,
                                       Root<BookCollection> collection,
                                       Join<BookCollection, User> user,
                                       Join<BookCollection, BookCollectionStats> collectionStats,
                                       Predicate predicate,
                                       Order order,
                                       Subquery<Long> booksCountSubquery) {

        query.select(cb.construct(
                        BookCollectionsAsPageDto.class,
                        collection.get(BookCollection_.id),
                        collection.get(BookCollection_.title),
                        collection.get(BookCollection_.description),
                        collection.get(BookCollection_.isPublic),
                        collection.get(BookCollection_.isDraft),
                        collectionStats.get(BookCollectionStats_.likesCount),
                        collectionStats.get(BookCollectionStats_.dislikesCount),
                        collectionStats.get(BookCollectionStats_.rating),
                        collectionStats.get(BookCollectionStats_.ratingsCount),
                        collectionStats.get(BookCollectionStats_.viewsCount),
                        collectionStats.get(AuditingEntityBase_.createdAt),
                        collectionStats.get(AuditingEntityBase_.updatedAt),
                        booksCountSubquery.getSelection().alias("booksCount"),
                        user.get(User_.id).alias("authorId"),
                        user.get(User_.username).alias("authorUsername")
                ))
                .where(predicate)
                .orderBy(order);
    }


    private Long countTotalCollections(CriteriaBuilder cb, BookCollectionFilterDto filter) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<BookCollection> collection = countQuery.from(BookCollection.class);
        Join<BookCollection, BookCollectionStats> collectionStats = collection.join(BookCollection_.bookCollectionStats);

        Subquery<Long> booksCountSubquery = null;
        if (filter.booksCount() != null) {
            booksCountSubquery = countQuery.subquery(Long.class);
            Root<BookCollectionItem> bookCollectionItem = booksCountSubquery.from(BookCollectionItem.class);
            booksCountSubquery.select(cb.count(bookCollectionItem))
                    .where(cb.equal(bookCollectionItem.get(BookCollectionItem_.bookCollection), collection));
        }

        Predicate predicate = createPredicate(filter, cb, collection, collectionStats, booksCountSubquery);

        countQuery
                .select(cb.countDistinct(collection.get(BookCollection_.id)))
                .where(predicate);
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private Predicate createPredicate(BookCollectionFilterDto filter,
                                      CriteriaBuilder cb,
                                      Root<BookCollection> collection,
                                      Join<BookCollection, BookCollectionStats> collectionStats,
                                      Subquery<Long> booksCountSubquery) {

        return PredicateBuilder.builder()
                .add(filter.title(), title -> cb.like(collection.get(BookCollection_.title), "%" + title + "%"))
                .add(filter.isPublic(), isPublic -> cb.equal(collection.get(BookCollection_.isPublic), isPublic))
                .add(filter.isDraft(), isDraft -> cb.equal(collection.get(BookCollection_.isDraft), isDraft))
                .add(filter.rating(), rating -> cb.greaterThanOrEqualTo(collectionStats.get(BookCollectionStats_.rating), rating))
                .add(filter.ratingsCount(), ratingsCount -> cb.greaterThanOrEqualTo(collectionStats.get(BookCollectionStats_.ratingsCount), ratingsCount))
                .add(filter.viewsCount(), viewsCount -> cb.greaterThanOrEqualTo(collectionStats.get(BookCollectionStats_.viewsCount), viewsCount))
                .add(filter.likesCount(), likesCount -> cb.greaterThanOrEqualTo(collectionStats.get(BookCollectionStats_.likesCount), likesCount))
                .add(filter.updatedAt(), updatedAt -> cb.greaterThanOrEqualTo(collectionStats.get(AuditingEntityBase_.updatedAt), updatedAt))
                .add(filter.createdAt(), createdAt -> cb.greaterThanOrEqualTo(collectionStats.get(AuditingEntityBase_.createdAt), createdAt))
                .add(filter.booksCount(), booksCount -> cb.greaterThanOrEqualTo(booksCountSubquery, booksCount))
                .buildAnd(cb);
    }


    private Order extractOrder(Pageable pageable, CriteriaBuilder cb, Join<BookCollection, BookCollectionStats> collectionStats) {
        Sort.Order sortOrder = pageable.getSort().get().findFirst().orElseThrow();
        String property = sortOrder.getProperty().replaceAll("[\\[\\]\"]", "");

        return switch (property) {
            case BookCollectionStats_.LIKES_COUNT -> sortOrder.isAscending()
                    ? cb.asc(collectionStats.get(BookCollectionStats_.likesCount))
                    : cb.desc(collectionStats.get(BookCollectionStats_.likesCount));
            case BookCollectionStats_.VIEWS_COUNT -> sortOrder.isAscending()
                    ? cb.asc(collectionStats.get(BookCollectionStats_.viewsCount))
                    : cb.desc(collectionStats.get(BookCollectionStats_.viewsCount));
            case BookCollectionStats_.RATING -> sortOrder.isAscending()
                    ? cb.asc(cb.coalesce(collectionStats.get(BookCollectionStats_.RATING), Integer.MAX_VALUE))
                    : cb.desc(cb.coalesce(collectionStats.get(BookCollectionStats_.RATING), Integer.MIN_VALUE));
            case AuditingEntityBase_.CREATED_AT -> sortOrder.isAscending()
                    ? cb.asc(collectionStats.get(AuditingEntityBase_.createdAt))
                    : cb.desc(collectionStats.get(AuditingEntityBase_.createdAt));
            case AuditingEntityBase_.UPDATED_AT -> sortOrder.isAscending()
                    ? cb.asc(cb.coalesce(collectionStats.get(AuditingEntityBase_.updatedAt), Instant.MAX))
                    : cb.desc(cb.coalesce(collectionStats.get(AuditingEntityBase_.updatedAt), Instant.MIN));
            default -> throw new NoSuchOrderByFieldException("Invalid sort property: " + property);
        };
    }

    private List<BookCollectionsAsPageDto> executeQueryWithPagination(Pageable pageable, CriteriaQuery<BookCollectionsAsPageDto> query) {
        return entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }
}
