package com.senla.readingbooks.service.interfaces;

import com.senla.readingbooks.dto.HtmlContentDto;
import com.senla.readingbooks.dto.comment.CommentReadDto;
import com.senla.readingbooks.dto.comment.CommentSaveDto;
import com.senla.readingbooks.dto.comment.EntityIdAndTypeDto;
import com.senla.readingbooks.dto.comment.RootCommentWithReplyAndUserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {
    CommentReadDto findCommentById(Long id);

    CommentReadDto createComment(CommentSaveDto dto);

    CommentReadDto updateComment(Long id, HtmlContentDto htmlContent);

    void deleteComment(Long id);

    CommentReadDto pinComment(Long id, EntityIdAndTypeDto dto);

    CommentReadDto unpinComment(Long id, EntityIdAndTypeDto entityIdAndTypeDto);

    Page<RootCommentWithReplyAndUserDto> findRootCommentsWithFirstReply(Pageable pageable, EntityIdAndTypeDto dto);

    boolean existsById(Long id);

    void checkAccessToEditComment(Long id);

    void checkAccessToPinUnpinComment(EntityIdAndTypeDto dto);
}

