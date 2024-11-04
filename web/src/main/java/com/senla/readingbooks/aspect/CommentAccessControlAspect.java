package com.senla.readingbooks.aspect;

import com.senla.readingbooks.dto.comment.EntityIdAndTypeDto;
import com.senla.readingbooks.service.interfaces.CommentService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class CommentAccessControlAspect extends CommonPointcut {
    private final CommentService commentService;

    @Pointcut("within(com.senla.readingbooks.controller.CommentController)")
    public void isCommentController() {
    }

    @Before("isCommentController() && isPutOrDeleteMappingMethod() && args(commentId, ..)")
    public void checkAccessToEditComment(Long commentId) {
        commentService.checkAccessToEditComment(commentId);
    }

    @Before("isCommentController() && isPatchMappingMethod() && args(*, dto)")
    public void checkAccessToPinUnpinComment(EntityIdAndTypeDto dto) {
        commentService.checkAccessToPinUnpinComment(dto);
    }

}

