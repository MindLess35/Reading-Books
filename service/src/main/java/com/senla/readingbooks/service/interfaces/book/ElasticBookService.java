package com.senla.readingbooks.service.interfaces.book;

import com.senla.readingbooks.dto.FullTextSearchDto;
import com.senla.readingbooks.dto.book.BooksFoundDto;
import com.senla.readingbooks.entity.book.Book;
import org.springframework.data.domain.Page;

public interface ElasticBookService {
    void saveBookDocument(Book book);

    void deleteById(Long id);

    Page<BooksFoundDto> searchByTitleOrAnnotation(FullTextSearchDto searchDto);
}
