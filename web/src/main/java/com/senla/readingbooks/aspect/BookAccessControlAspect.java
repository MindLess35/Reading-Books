package com.senla.readingbooks.aspect;

import com.senla.readingbooks.service.interfaces.book.BookService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class BookAccessControlAspect extends CommonPointcut {
    private final BookService bookService;

    @Pointcut("within(com.senla.readingbooks.controller.book.BookController)")
    public void isBookController() {
    }

    @Before("isBookController() && isPutOrPatchOrDeleteMappingMethod() && args(bookId, ..)")
    public void checkAccessToBook(Long bookId) {
        bookService.checkAccessToBook(bookId);
    }

}

