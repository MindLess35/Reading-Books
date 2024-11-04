package com.senla.readingbooks.controller.user;

import com.senla.readingbooks.dto.UserLibraryAsPageDto;
import com.senla.readingbooks.dto.UserLibraryFilterDto;
import com.senla.readingbooks.dto.user.UserLibraryAddDto;
import com.senla.readingbooks.dto.user.UserLibraryEditDto;
import com.senla.readingbooks.enums.book.LibrarySection;
import com.senla.readingbooks.service.interfaces.user.UserLibraryService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/libraries")
public class UserLibraryController {
    private final UserLibraryService userLibraryService;

    @PreAuthorize("T(java.lang.Long).valueOf(principal) == #dto.userId()")
    @PostMapping
    public ResponseEntity<HttpStatus> addBookToLibrary(@RequestBody @Validated UserLibraryAddDto dto) {
        userLibraryService.addBookToLibrary(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<Page<UserLibraryAsPageDto>> getUserLibraryBooks(
            @PageableDefault(sort = "additionDate", direction = Sort.Direction.DESC) Pageable pageable,
            @Validated UserLibraryFilterDto filter) {

        Page<UserLibraryAsPageDto> booksPage = userLibraryService.findBooksInLibrary(filter, pageable);
        return ResponseEntity.ok(booksPage);
    }

    @PreAuthorize("T(java.lang.Long).valueOf(principal) == #dto.userId()")
    @DeleteMapping
    public ResponseEntity<HttpStatus> deleteBookFromLibrary(@Validated UserLibraryEditDto dto) {
        userLibraryService.deleteBookFromLibrary(dto);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("T(java.lang.Long).valueOf(principal) == #dto.userId()")
    @PutMapping
    public ResponseEntity<HttpStatus> updateLibrarySection(@Validated UserLibraryEditDto dto, @RequestParam LibrarySection section) {
        userLibraryService.updateLibrarySection(dto, section);
        return ResponseEntity.ok().build();
    }

}