package com.senla.readingbooks.repository.criteria;

import com.senla.readingbooks.dto.UserLibraryAsPageDto;
import com.senla.readingbooks.dto.UserLibraryFilterDto;
import com.senla.readingbooks.entity.book.Book;
import com.senla.readingbooks.entity.book.Book_;
import com.senla.readingbooks.entity.user.User;
import com.senla.readingbooks.entity.user.UserLibrary;
import com.senla.readingbooks.entity.user.UserLibrary_;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

@RequiredArgsConstructor
public class CriteriaUserLibraryRepositoryImpl implements CriteriaUserLibraryRepository {
    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public Page<UserLibraryAsPageDto> findUserLibrariesAsPage(UserLibraryFilterDto filter, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserLibraryAsPageDto> query = cb.createQuery(UserLibraryAsPageDto.class);

        Root<UserLibrary> library = query.from(UserLibrary.class);
        Join<UserLibrary, User> user = library.join(UserLibrary_.user);
        Join<UserLibrary, Book> book = library.join(UserLibrary_.book);
        Order order = extractOrder(pageable, cb, library, book, user);

        Long total = countTotalBookInLibrary(cb, filter);
        if (total == 0) {
            return Page.empty(pageable);
        }

        Predicate predicate = createPredicate(filter, cb, library, book);
        constructQuery(query, cb, book, user, predicate, order);

        List<UserLibraryAsPageDto> resultList = executeQueryWithPagination(pageable, query);
        return new PageImpl<>(resultList, pageable, total);
    }

    private static void constructQuery(CriteriaQuery<UserLibraryAsPageDto> query,
                                       CriteriaBuilder cb,
                                       Join<UserLibrary, Book> book,
                                       Join<UserLibrary, User> user,
                                       Predicate predicate,
                                       Order order) {
        query.select(cb.construct(
                        UserLibraryAsPageDto.class,
                        book.get(Book_.id),
                        book.get(Book_.title),
                        book.get(Book_.coverUrl),
                        user.get(User_.id),
                        user.get(User_.username)
                ))
                .where(predicate)
                .orderBy(order);
    }

    private Long countTotalBookInLibrary(CriteriaBuilder cb, UserLibraryFilterDto filter) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<UserLibrary> library = countQuery.from(UserLibrary.class);
        Join<UserLibrary, Book> book = library.join(UserLibrary_.book);

        Predicate predicate = createPredicate(filter, cb, library, book);

        countQuery
                .select(cb.countDistinct(library.get(UserLibrary_.id)))
                .where(predicate);
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private Predicate createPredicate(UserLibraryFilterDto filter,
                                      CriteriaBuilder cb,
                                      Root<UserLibrary> library,
                                      Join<UserLibrary, Book> book) {
        return PredicateBuilder.builder()
                .add(filter.userId(), userId -> cb.equal(library.get(UserLibrary_.user).get(User_.id), userId))
                .add(filter.section(), section -> cb.equal(library.get(UserLibrary_.section), section))
                .add(filter.form(), form -> cb.equal(book.get(Book_.form), form))
                .add(filter.status(), status -> cb.equal(book.get(Book_.status), status))
                .add(filter.genre(), genre -> cb.isMember(genre, book.get(Book_.genres)))
                .buildAnd(cb);
    }

    private Order extractOrder(Pageable pageable,
                               CriteriaBuilder cb,
                               Root<UserLibrary> library,
                               Join<UserLibrary, Book> book,
                               Join<UserLibrary, User> user) {
        Sort.Order sortOrder = pageable.getSort().get().findFirst().orElseThrow();
        String property = sortOrder.getProperty().replaceAll("[\\[\\]\"]", "");

        return switch (property) {
            case Book_.TITLE -> sortOrder.isAscending()
                    ? cb.asc(book.get(Book_.title))
                    : cb.desc(book.get(Book_.title));
            case UserLibrary_.ADDITION_DATE -> sortOrder.isAscending()
                    ? cb.asc(library.get(UserLibrary_.additionDate))
                    : cb.desc(library.get(UserLibrary_.additionDate));
            case User_.USERNAME -> sortOrder.isAscending()
                    ? cb.asc(user.get(User_.username))
                    : cb.desc(user.get(User_.username));
            default -> throw new NoSuchOrderByFieldException("Invalid sort property: " + property);
        };
    }

    private List<UserLibraryAsPageDto> executeQueryWithPagination(Pageable pageable, CriteriaQuery<UserLibraryAsPageDto> query) {
        return entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }
}
