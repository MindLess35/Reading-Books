package com.senla.readingbooks.service.impl.book;

import com.senla.readingbooks.entity.collection.BookCollection;
import com.senla.readingbooks.entity.collection.BookCollectionStats;
import com.senla.readingbooks.repository.jpa.book.BookCollectionStatsRepository;
import com.senla.readingbooks.service.interfaces.book.BookCollectionStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookCollectionStatsServiceImpl implements BookCollectionStatsService {
    private final BookCollectionStatsRepository bookCollectionStatsRepository;

    @Override
    public void createBookCollectionStats(BookCollection bookCollection) {
        BookCollectionStats.BookCollectionStatsBuilder bookCollectionStatsBuilder = BookCollectionStats.builder()
                .bookCollection(bookCollection);
        bookCollectionStatsRepository.save(bookCollectionStatsBuilder.build());
    }

    @Override
    @Transactional
    public void updateBookCollectionStats(Long bookCollectionId) {
        bookCollectionStatsRepository.updateStatistics(bookCollectionId, Instant.now());
    }
}