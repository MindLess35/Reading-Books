package com.senla.readingbooks.repository.criteria;

import com.senla.readingbooks.dto.UserLibraryAsPageDto;
import com.senla.readingbooks.dto.UserLibraryFilterDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CriteriaUserLibraryRepository {
    Page<UserLibraryAsPageDto> findUserLibrariesAsPage(UserLibraryFilterDto filter, Pageable pageable);

}
