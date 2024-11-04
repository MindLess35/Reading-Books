package com.senla.readingbooks.service.interfaces.book;

import com.senla.readingbooks.dto.ChapterReadDto;
import com.senla.readingbooks.dto.ChapterSaveDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChapterService {
    ChapterReadDto findChapterById(Long id);

    ChapterReadDto updateChapter(Long id, ChapterSaveDto chapterSaveDto);

    void deleteChapter(Long id);

    ChapterReadDto createChapter(ChapterSaveDto chapterSaveDto);

    ChapterReadDto publishChapter(Long chapterId);

    boolean existsById(Long id);

    Page<ChapterReadDto> findAllChaptersByBookId(Long bookId, Pageable pageable);

    void checkAccessToChapter(Long chapterId);

    void checkAccessToCreateChapter(Long bookId);

    boolean isUserIsAuthorOfChapter(Long chapterId, Long userId);
}
