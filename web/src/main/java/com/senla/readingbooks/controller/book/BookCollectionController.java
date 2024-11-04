package com.senla.readingbooks.controller.book;

import com.senla.readingbooks.dto.BookCollectionFilterDto;
import com.senla.readingbooks.dto.BookCollectionsAsPageDto;
import com.senla.readingbooks.dto.FullTextSearchDto;
import com.senla.readingbooks.dto.bookcollection.AddBookToCollectionDto;
import com.senla.readingbooks.dto.bookcollection.BookCollectionReadDto;
import com.senla.readingbooks.dto.bookcollection.BookCollectionSaveDto;
import com.senla.readingbooks.dto.bookcollection.BookCollectionsFoundDto;
import com.senla.readingbooks.service.interfaces.book.BookCollectionService;
import com.senla.readingbooks.service.interfaces.book.ElasticBookCollectionService;
import com.senla.readingbooks.validation.group.OnCreate;
import com.senla.readingbooks.validation.group.OnUpdate;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/collections")
public class BookCollectionController {
    private final BookCollectionService bookCollectionService;
    private final ElasticBookCollectionService elasticBookCollectionService;

    @GetMapping("/{id}")
    public ResponseEntity<BookCollectionReadDto> getBookCollection(@PathVariable("id") Long id) {
        return ResponseEntity.ok(bookCollectionService.findBookCollectionById(id));
    }

    @GetMapping
    public ResponseEntity<Page<BookCollectionsAsPageDto>> getAllBookCollectionsByFilter(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC, size = 25) Pageable pageable,
            @Validated BookCollectionFilterDto filter) {

        return ResponseEntity.ok(bookCollectionService.findBookCollectionsAsPage(pageable, filter));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<BookCollectionsFoundDto>> search(@Validated FullTextSearchDto searchDto) {
        return ResponseEntity.ok(elasticBookCollectionService.searchByTitleOrDescription(searchDto));
    }

    @PreAuthorize("T(java.lang.Long).valueOf(principal) == #bookCollectionSaveDto.userId()")
    @PostMapping
    public ResponseEntity<BookCollectionReadDto> createBookCollection(
            @RequestBody @Validated({Default.class, OnCreate.class}) BookCollectionSaveDto bookCollectionSaveDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookCollectionService.createBookCollection(bookCollectionSaveDto));
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<BookCollectionReadDto> publishBookCollection(@PathVariable Long id) {
        return ResponseEntity.ok(bookCollectionService.publishCollection(id));
    }

    @PostMapping("/{id}/book/{bookId}")
    public ResponseEntity<HttpStatus> addBookToCollection(@PathVariable Long id,
                                                          @PathVariable Long bookId,
                                                          @RequestBody AddBookToCollectionDto dto) {
        bookCollectionService.addBookToCollection(id, bookId, dto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookCollectionReadDto> updateBookCollection(
            @PathVariable("id") Long id,
            @RequestBody @Validated({Default.class, OnUpdate.class}) BookCollectionSaveDto bookCollectionSaveDto) {
        return ResponseEntity.ok(bookCollectionService.updateBookCollection(id, bookCollectionSaveDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteBookCollection(@PathVariable("id") Long id) {
        bookCollectionService.deleteBookCollection(id);
        return ResponseEntity.noContent().build();
    }
}
