package com.senla.readingbooks.service.interfaces.book;

import com.senla.readingbooks.dto.BookFilterDto;
import com.senla.readingbooks.dto.BooksAsPageDto;
import com.senla.readingbooks.dto.book.BookReadDto;
import com.senla.readingbooks.dto.book.BookSaveDto;
import com.senla.readingbooks.entity.book.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookService {
    BookReadDto findByIdEager(Long id);

    BookReadDto updateBook(Long id, BookSaveDto dto);

    void deleteBook(Long id);

    BookReadDto createBook(BookSaveDto bookSaveDto);

    BookReadDto publishBook(Long id);

    Book findByIdLazy(Long id);

    boolean existsById(Long id);

    Page<BooksAsPageDto> findBooksAsPage(Pageable pageable, BookFilterDto filter);

    void checkAccessToBook(Long id);

    boolean isUserIsAuthorOfBook(Long bookId, Long userId);
}
