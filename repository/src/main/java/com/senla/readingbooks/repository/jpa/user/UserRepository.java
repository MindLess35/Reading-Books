package com.senla.readingbooks.repository.jpa.user;

import com.senla.readingbooks.entity.user.User;
import com.senla.readingbooks.enums.user.Role;
import com.senla.readingbooks.projection.EntityIdWithImageUrlProjection;
import com.senla.readingbooks.projection.UserWithBooksCountProjection;
import com.senla.readingbooks.repository.criteria.CriteriaUserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, CriteriaUserRepository {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByUsername(String username);

    List<User> findUsersByIdIn(List<Long> ids);

    @Modifying
    @Query("""
            UPDATE User u
            SET u.avatarUrl = :avatarUrl
            WHERE u.id = :userId
            """)
    void updateAvatarById(String avatarUrl, Long userId);

    @Modifying
    @Query("""
            UPDATE User u
            SET u.avatarUrl = null
            WHERE u.id = :userId
            """)
    void updateAvatarToNullById(Long userId);

    @Query("""
            SELECT u.avatarUrl AS imageUrl,
                   u.id AS entityId
            FROM User u
            WHERE u.id = :userId
            """)
    Optional<EntityIdWithImageUrlProjection> findAvatarWithUserIdById(Long userId);


    @Query(value = """
            SELECT DISTINCT
                   u.id AS id,
                   u.username AS username,
                   u.avatar_url AS avatarUrl,
                   COUNT(ba.book_id) OVER (PARTITION BY ba.author_id) AS booksWrittenCount
            FROM users u
            LEFT JOIN book_authors ba ON u.id = ba.author_id
            WHERE u.id IN (:userIds);
            """, nativeQuery = true)
    List<UserWithBooksCountProjection> findUserDetailsWithBookCount(List<Long> userIds);

    @Modifying
    @Query("""
            UPDATE User u
            SET u.password = :newPassword
            WHERE u.id = :id
            """)
    void updatePassword(Long id, String newPassword);

    @Modifying
    @Query("""
            UPDATE User u
            SET u.role = :role
            WHERE u.id IN :userIds
            """)
    void makeUsersAuthors(List<Long> userIds, Role role);

    Optional<User> findByRole(Role role);
}