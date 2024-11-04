package com.senla.readingbooks.aspect;

import com.senla.readingbooks.dto.ChapterSaveDto;
import com.senla.readingbooks.service.interfaces.book.ChapterService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class ChapterAccessControlAspect extends CommonPointcut {
    private final ChapterService chapterService;

    @Pointcut("within(com.senla.readingbooks.controller.book.ChapterController)")
    public void isChapterController() {
    }

    @Before("isChapterController() && isPutOrPatchOrDeleteMappingMethod() && args(chapterId, ..)")
    public void checkAccessToChapter(Long chapterId) {
        chapterService.checkAccessToChapter(chapterId);
    }

    @Before("isChapterController() && isPostMappingMethod() && args(chapterDto)")
    public void checkAccessToCreateChapter(ChapterSaveDto chapterDto) {
        chapterService.checkAccessToCreateChapter(chapterDto.bookId());
    }

}

