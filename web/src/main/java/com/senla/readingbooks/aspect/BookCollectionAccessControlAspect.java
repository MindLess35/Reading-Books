package com.senla.readingbooks.aspect;

import com.senla.readingbooks.service.interfaces.book.BookCollectionService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class BookCollectionAccessControlAspect extends CommonPointcut {
    private final BookCollectionService bookCollectionService;

    @Pointcut("within(com.senla.readingbooks.controller.book.BookCollectionController)")
    public void isBookCollectionController() {
    }

    @Before("isBookCollectionController() && (isPutOrPatchOrDeleteMappingMethod() || isPostMappingMethod()) && args(collectionId, ..)")
    public void checkAccessToBookCollection(Long collectionId) {
        bookCollectionService.checkAccessToCollection(collectionId);
    }

}

