package com.senla.readingbooks.service.interfaces.book;

import com.senla.readingbooks.dto.BookReviewReadDto;
import com.senla.readingbooks.dto.BookReviewSaveDto;

public interface BookReviewService {
    BookReviewReadDto findReviewById(Long id);

    BookReviewReadDto createReview(BookReviewSaveDto dto);

    BookReviewReadDto updateReview(Long id, BookReviewSaveDto dto);

    void deleteReview(Long id);

    boolean existsById(Long id);

    void checkAccessToEditReview(Long reviewId);

    boolean isUserIsAuthorOfReview(Long reviewId, Long userId);
}
