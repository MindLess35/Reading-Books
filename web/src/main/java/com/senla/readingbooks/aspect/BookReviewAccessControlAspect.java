package com.senla.readingbooks.aspect;

import com.senla.readingbooks.service.interfaces.book.BookReviewService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class BookReviewAccessControlAspect extends CommonPointcut {
    private final BookReviewService bookReviewService;

    @Pointcut("within(com.senla.readingbooks.controller.book.BookReviewController)")
    public void isBookReviewController() {
    }

    @Before("isBookReviewController() && isPutOrDeleteMappingMethod() && args(reviewId, ..)")
    public void checkAccessToEditBookReview(Long reviewId) {
        bookReviewService.checkAccessToEditReview(reviewId);
    }

}

