package com.senla.readingbooks.repository.criteria;

import com.senla.readingbooks.dto.UserFilterDto;
import com.senla.readingbooks.dto.UsersAsPageDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CriteriaUserRepository {
    Page<UsersAsPageDto> findUsersAsPage(Pageable pageable, UserFilterDto filter);
}
