package com.senla.readingbooks.aspect;

import com.senla.readingbooks.service.interfaces.book.BookSeriesService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class BookSeriesAccessControlAspect extends CommonPointcut {
    private final BookSeriesService bookSeriesService;

    @Pointcut("within(com.senla.readingbooks.controller.book.BookSeriesController)")
    public void isBookSeriesController() {
    }

    @Before("isBookSeriesController() && isPutOrPatchOrDeleteMappingMethod() && args(seriesId, ..)")
    public void checkAccessToBookSeries(Long seriesId) {
        bookSeriesService.checkAccessToSeries(seriesId);
    }

}

