package com.senla.readingbooks.repository.criteria;

import com.senla.readingbooks.dto.BookCollectionFilterDto;
import com.senla.readingbooks.dto.BookCollectionsAsPageDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CriteriaBookCollectionRepository {
    Page<BookCollectionsAsPageDto> findBookCollectionsAsPage(Pageable pageable, BookCollectionFilterDto filter);
}
