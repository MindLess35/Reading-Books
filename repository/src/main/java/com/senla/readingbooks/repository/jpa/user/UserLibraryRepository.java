package com.senla.readingbooks.repository.jpa.user;

import com.senla.readingbooks.entity.user.UserLibrary;
import com.senla.readingbooks.repository.criteria.CriteriaUserLibraryRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserLibraryRepository extends
        JpaRepository<UserLibrary, Long>,
        CriteriaUserLibraryRepository {

    Optional<UserLibrary> findByUserIdAndBookId(Long userId, Long bookId);

    boolean existsByUserIdAndBookId(Long userId, Long bookId);
}
