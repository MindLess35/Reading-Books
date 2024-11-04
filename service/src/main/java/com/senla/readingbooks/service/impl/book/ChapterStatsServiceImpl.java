package com.senla.readingbooks.service.impl.book;


import com.senla.readingbooks.entity.book.Chapter;
import com.senla.readingbooks.entity.book.ChapterStatistics;
import com.senla.readingbooks.repository.jpa.book.ChapterStatsRepository;
import com.senla.readingbooks.service.interfaces.book.ChapterStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChapterStatsServiceImpl implements ChapterStatsService {
    private final ChapterStatsRepository chapterStatsRepository;

    @Override
    public void createChapterStatistics(Chapter chapter) {
        ChapterStatistics chapterStatistics = ChapterStatistics
                .builder()
                .chapter(chapter)
                .build();
        chapterStatsRepository.save(chapterStatistics);

    }

    @Override
    @Transactional
    public void updateChapterStatistics(Long chapterId) {
        chapterStatsRepository.updateStatistics(chapterId, Instant.now());
    }
}
