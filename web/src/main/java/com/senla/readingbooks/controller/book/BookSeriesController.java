package com.senla.readingbooks.controller.book;

import com.senla.readingbooks.dto.BookSeriesReadDto;
import com.senla.readingbooks.dto.BookSeriesSaveDto;
import com.senla.readingbooks.service.interfaces.book.BookSeriesService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/series")
public class BookSeriesController {
    private final BookSeriesService bookSeriesService;

    @GetMapping("/{id}")
    public ResponseEntity<BookSeriesReadDto> getBookSeries(@PathVariable("id") Long id) {
        return ResponseEntity.ok(bookSeriesService.findBookSeriesById(id));
    }

    @PreAuthorize("T(java.lang.Long).valueOf(principal) == #bookSeriesSaveDto.userId()")
    @PostMapping
    public ResponseEntity<BookSeriesReadDto> createBookSeries(@RequestBody @Validated({Default.class, OnCreate.class}) BookSeriesSaveDto bookSeriesSaveDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookSeriesService.createBookSeries(bookSeriesSaveDto));
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<BookSeriesReadDto> publishBookSeries(@PathVariable Long id) {
        return ResponseEntity.ok(bookSeriesService.publishSeries(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookSeriesReadDto> updateBookSeries(@PathVariable("id") Long id,
                                                              @RequestBody @Validated({Default.class, OnUpdate.class}) BookSeriesSaveDto bookSeriesSaveDto) {
        return ResponseEntity.ok(bookSeriesService.updateBookSeries(id, bookSeriesSaveDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteBookSeries(@PathVariable("id") Long id) {
        bookSeriesService.deleteBookSeries(id);
        return ResponseEntity.noContent().build();
    }
}
