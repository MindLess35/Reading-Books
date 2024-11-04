package com.senla.readingbooks.service.interfaces.book;

import com.senla.readingbooks.entity.book.Chapter;

public interface ChapterStatsService {
    void createChapterStatistics(Chapter chapter);

    void updateChapterStatistics(Long chapterId);

}
