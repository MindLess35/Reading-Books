package com.senla.readingbooks.service.interfaces.user;

import com.senla.readingbooks.dto.FullTextSearchDto;
import com.senla.readingbooks.entity.user.User;
import com.senla.readingbooks.projection.UserWithBooksCountProjection;
import org.springframework.data.domain.Page;

public interface ElasticUserService {
    void saveUserDocument(User user);

    void deleteById(Long id);

    Page<UserWithBooksCountProjection> searchByUsername(FullTextSearchDto searchDto);
}