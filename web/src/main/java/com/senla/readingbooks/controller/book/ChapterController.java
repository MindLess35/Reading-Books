package com.senla.readingbooks.controller.book;

import com.senla.readingbooks.dto.ChapterReadDto;
import com.senla.readingbooks.dto.ChapterSaveDto;
import com.senla.readingbooks.service.interfaces.book.ChapterService;
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
@RequestMapping("/api/v1/chapters")
public class ChapterController {
    private final ChapterService chapterService;

    @GetMapping("/{id}")
    public ResponseEntity<ChapterReadDto> getChapter(@PathVariable("id") Long id) {
        return ResponseEntity.ok(chapterService.findChapterById(id));
    }

    @GetMapping("/book/{bookId}")
    public Page<ChapterReadDto> getChaptersByBookId(
            @PathVariable Long bookId,
            @PageableDefault(sort = "publicationDate", direction = Sort.Direction.ASC, size = 100) Pageable pageable) {
        return chapterService.findAllChaptersByBookId(bookId, pageable);
    }

    @PostMapping
    public ResponseEntity<ChapterReadDto> createChapter(
            @RequestBody @Validated({Default.class, OnCreate.class}) ChapterSaveDto chapterCreateEditDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chapterService.createChapter(chapterCreateEditDto));
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<ChapterReadDto> publishChapter(@PathVariable Long id) {
        return ResponseEntity.ok(chapterService.publishChapter(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChapterReadDto> updateChapter(
            @PathVariable("id") Long id,
            @RequestBody @Validated({Default.class, OnUpdate.class}) ChapterSaveDto chapterCreateEditDto) {
        return ResponseEntity.ok(chapterService.updateChapter(id, chapterCreateEditDto));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<HttpStatus> deleteChapter(@PathVariable("id") Long id) {
        chapterService.deleteChapter(id);
        return ResponseEntity.noContent().build();
    }

}
