package com.senla.readingbooks.controller.book;

import com.senla.readingbooks.dto.BookReviewReadDto;
import com.senla.readingbooks.dto.BookReviewSaveDto;
import com.senla.readingbooks.service.interfaces.book.BookReviewService;
import com.senla.readingbooks.validation.group.OnCreate;
import com.senla.readingbooks.validation.group.OnUpdate;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
public class BookReviewController {
    private final BookReviewService bookReviewService;

    @GetMapping("/{id}")
    public ResponseEntity<BookReviewReadDto> getReview(@PathVariable("id") Long id) {
        return ResponseEntity.ok(bookReviewService.findReviewById(id));
    }

    @PreAuthorize("T(java.lang.Long).valueOf(principal) == #bookReviewSaveDto.userId()")
    @PostMapping
    public ResponseEntity<BookReviewReadDto> createReview(
            @RequestBody @Validated({Default.class, OnCreate.class}) BookReviewSaveDto bookReviewSaveDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookReviewService.createReview(bookReviewSaveDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookReviewReadDto> updateReview(
            @PathVariable("id") Long id,
            @RequestBody @Validated({Default.class, OnUpdate.class}) BookReviewSaveDto bookReviewSaveDto) {
        return ResponseEntity.ok(bookReviewService.updateReview(id, bookReviewSaveDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteReview(@PathVariable("id") Long id) {
        bookReviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}
