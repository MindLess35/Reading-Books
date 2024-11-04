package com.senla.readingbooks.repository.criteria;

import com.senla.readingbooks.dto.UserFilterDto;
import com.senla.readingbooks.dto.UsersAsPageDto;
import com.senla.readingbooks.entity.AuditingEntityBase_;
import com.senla.readingbooks.entity.book.Book;
import com.senla.readingbooks.entity.book.Book_;
import com.senla.readingbooks.entity.user.User;
import com.senla.readingbooks.entity.user.User_;
import com.senla.readingbooks.exception.NoSuchOrderByFieldException;
import com.senla.readingbooks.util.PredicateBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
public class CriteriaUserRepositoryImpl implements CriteriaUserRepository {
    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public Page<UsersAsPageDto> findUsersAsPage(Pageable pageable, UserFilterDto filter) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<UsersAsPageDto> query = cb.createQuery(UsersAsPageDto.class);
        Root<User> user = query.from(User.class);
        Order order = extractOrder(pageable, cb, user);

        Long total = countTotalUsers(cb, filter);
        if (total == 0) {
            return Page.empty(pageable);
        }

        Subquery<Long> booksCountSubquery = query.subquery(Long.class);
        Root<Book> bookRoot = booksCountSubquery.from(Book.class);

        booksCountSubquery.select(cb.count(bookRoot))
                .where(cb.equal(bookRoot.join(Book_.users), user));

        Predicate predicate = createPredicate(filter, cb, user, booksCountSubquery);
        constructQuery(query, cb, user, booksCountSubquery, predicate, order);

        List<UsersAsPageDto> resultList = executeQueryWithPagination(pageable, query);
        return new PageImpl<>(resultList, pageable, total);
    }

    private static void constructQuery(CriteriaQuery<UsersAsPageDto> query, CriteriaBuilder cb, Root<User> user, Subquery<Long> booksCountSubquery, Predicate predicate, Order order) {
        query.select(cb.construct(
                        UsersAsPageDto.class,
                        user.get(User_.id),
                        user.get(User_.username),
                        user.get(User_.email),
                        user.get(User_.status),
                        user.get(User_.about),
                        user.get(User_.gender),
                        user.get(AuditingEntityBase_.createdAt),
                        user.get(AuditingEntityBase_.updatedAt),
                        user.get(User_.birthDate),
                        user.get(User_.role),
                        user.get(User_.avatarUrl),
                        booksCountSubquery.getSelection().alias("booksWrittenCount")))
                .where(predicate)
                .orderBy(order);
    }

    private Long countTotalUsers(CriteriaBuilder cb, UserFilterDto filter) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<User> user = countQuery.from(User.class);
        Subquery<Long> booksCountSubquery = countQuery.subquery(Long.class);
        Root<Book> bookRoot = booksCountSubquery.from(Book.class);

        booksCountSubquery.select(cb.count(bookRoot))
                .where(cb.equal(bookRoot.join(Book_.users), user));

        Predicate predicate = createPredicate(filter, cb, user, booksCountSubquery);
        countQuery.select(cb.countDistinct(user.get(User_.id)))
                .where(predicate);

        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private Predicate createPredicate(UserFilterDto filter,
                                      CriteriaBuilder cb,
                                      Root<User> user,
                                      Subquery<Long> booksCountSubquery) {

        return PredicateBuilder.builder()
                .add(filter.username(), username -> cb.like(user.get(User_.username), "%" + username + "%"))
                .add(filter.email(), email -> cb.like(user.get(User_.email), "%" + email + "%"))
                .add(filter.status(), status -> cb.like(user.get(User_.status), "%" + status + "%"))
                .add(filter.about(), about -> cb.like(user.get(User_.about), "%" + about + "%"))
                .add(filter.birthDate(), birthDate -> cb.greaterThanOrEqualTo(user.get(User_.birthDate), birthDate))
                .add(filter.gender(), gender -> cb.equal(user.get(User_.gender), gender))
                .add(filter.role(), role -> cb.equal(user.get(User_.role), role))
                .add(filter.isHasAvatar(), isHasAvatar -> isHasAvatar
                        ? cb.isNotNull(user.get(User_.avatarUrl))
                        : cb.isNull(user.get(User_.avatarUrl)))

                .add(filter.createdAt(), createdAt -> cb.greaterThanOrEqualTo(user.get(AuditingEntityBase_.createdAt), createdAt))
                .add(filter.updatedAt(), updatedAt -> cb.greaterThanOrEqualTo(user.get(AuditingEntityBase_.updatedAt), updatedAt))
                .add(filter.booksWrittenCount(), booksWrittenCount -> cb.greaterThanOrEqualTo(booksCountSubquery, booksWrittenCount))
                .buildAnd(cb);
    }

    private List<UsersAsPageDto> executeQueryWithPagination(Pageable pageable, CriteriaQuery<UsersAsPageDto> query) {
        return entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }

    private Order extractOrder(Pageable pageable, CriteriaBuilder cb, Root<User> user) {
        Sort.Order sortOrder = pageable.getSort().get().findFirst().orElseThrow();
        String property = sortOrder.getProperty().replaceAll("[\\[\\]\"]", "");

        return switch (property) {
            case User_.USERNAME -> sortOrder.isAscending()
                    ? cb.asc(user.get(User_.USERNAME))
                    : cb.desc(user.get(User_.USERNAME));
            case User_.EMAIL -> sortOrder.isAscending()
                    ? cb.asc(user.get(User_.EMAIL))
                    : cb.desc(user.get(User_.EMAIL));
            case User_.BIRTH_DATE -> sortOrder.isAscending()
                    ? cb.asc(cb.coalesce(user.get(User_.BIRTH_DATE), LocalDate.MAX))
                    : cb.desc(cb.coalesce(user.get(User_.BIRTH_DATE), LocalDate.MIN));
            case AuditingEntityBase_.CREATED_AT -> sortOrder.isAscending()
                    ? cb.asc(user.get(AuditingEntityBase_.CREATED_AT))
                    : cb.desc(user.get(AuditingEntityBase_.CREATED_AT));
            default -> throw new NoSuchOrderByFieldException("Invalid sort property: " + property);
        };
    }

}

