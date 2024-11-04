package com.senla.readingbooks.controller.book;

import com.senla.readingbooks.dto.BookFilterDto;
import com.senla.readingbooks.dto.BooksAsPageDto;
import com.senla.readingbooks.dto.FullTextSearchDto;
import com.senla.readingbooks.dto.book.BookReadDto;
import com.senla.readingbooks.dto.book.BookSaveDto;
import com.senla.readingbooks.dto.book.BooksFoundDto;
import com.senla.readingbooks.service.interfaces.book.BookService;
import com.senla.readingbooks.service.interfaces.book.ElasticBookService;
import com.senla.readingbooks.validation.group.OnCreate;
import com.senla.readingbooks.validation.group.OnUpdate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Book Controller", description = "Controller for working with the book")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/books")
public class BookController {
    private final BookService bookService;
    private final ElasticBookService elasticBookService;

    @GetMapping("/{id}")
    public ResponseEntity<BookReadDto> getBook(@PathVariable("id") Long id) {
        return ResponseEntity.ok(bookService.findByIdEager(id));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<BooksFoundDto>> search(@Validated FullTextSearchDto searchDto) {
        return ResponseEntity.ok(elasticBookService.searchByTitleOrAnnotation(searchDto));
    }

    @GetMapping
    public ResponseEntity<Page<BooksAsPageDto>> getAllBooksByFilter(
            @PageableDefault(sort = "viewsCount", direction = Sort.Direction.DESC, size = 25) Pageable pageable,
            @Validated BookFilterDto filter) {

        return ResponseEntity.ok(bookService.findBooksAsPage(pageable, filter));
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<BookReadDto> publishBook(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.publishBook(id));
    }

    @Operation(summary = "You must refresh the access token so that the user role is updated to the \"AUTHOR\" after the book creation")
    @PreAuthorize("#bookSaveDto.authorIds.contains(T(java.lang.Long).valueOf(principal))")
    @PostMapping
    public ResponseEntity<BookReadDto> createBook(
            @RequestBody @Validated({Default.class, OnCreate.class}) BookSaveDto bookSaveDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.createBook(bookSaveDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookReadDto> updateBook(
            @PathVariable("id") Long id,
            @RequestBody @Validated({Default.class, OnUpdate.class}) BookSaveDto bookSaveDto) {
        return ResponseEntity.ok(bookService.updateBook(id, bookSaveDto));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<HttpStatus> deleteBook(@PathVariable("id") Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

}