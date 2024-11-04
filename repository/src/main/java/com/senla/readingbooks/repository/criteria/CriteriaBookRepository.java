package com.senla.readingbooks.repository.criteria;

import com.senla.readingbooks.dto.BookFilterDto;
import com.senla.readingbooks.dto.BooksAsPageDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CriteriaBookRepository {
    Page<BooksAsPageDto> findBooksAsPage(Pageable pageable, BookFilterDto filter);

}
