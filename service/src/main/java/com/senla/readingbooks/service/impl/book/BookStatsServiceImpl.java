package com.senla.readingbooks.service.impl.book;

import com.senla.readingbooks.entity.book.BookStatistics;
import com.senla.readingbooks.event.BookCreatedEvent;
import com.senla.readingbooks.repository.jpa.book.BookStatsRepository;
import com.senla.readingbooks.service.interfaces.book.BookStatsService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookStatsServiceImpl implements BookStatsService {
    private final BookStatsRepository bookStatsRepository;
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    @EventListener
    public void createBookStatistics(BookCreatedEvent event) {
        BookStatistics bookStatistics = BookStatistics.builder()
                .book(event.book())
                .build();
        entityManager.persist(bookStatistics);
    }

    @Override
    @Transactional
    public void updateBookStatistics(Long bookId) {
        bookStatsRepository.updateStatistics(bookId, Instant.now());
    }

    @Transactional
    @Override
    public void updatePublicationDate(Long bookId) {
        bookStatsRepository.updatePublicationDate(bookId, Instant.now());
    }
}
