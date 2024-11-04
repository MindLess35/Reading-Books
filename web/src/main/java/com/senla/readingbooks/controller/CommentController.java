package com.senla.readingbooks.controller;

import com.senla.readingbooks.dto.HtmlContentDto;
import com.senla.readingbooks.dto.comment.CommentReadDto;
import com.senla.readingbooks.dto.comment.CommentSaveDto;
import com.senla.readingbooks.dto.comment.EntityIdAndTypeDto;
import com.senla.readingbooks.dto.comment.RootCommentWithReplyAndUserDto;
import com.senla.readingbooks.service.interfaces.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/comments")
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/{id}")
    public ResponseEntity<CommentReadDto> getComment(@PathVariable("id") Long id) {
        return ResponseEntity.ok(commentService.findCommentById(id));
    }

    @GetMapping
    public ResponseEntity<Page<RootCommentWithReplyAndUserDto>> findRootCommentsWithFirstReply(
            @Validated EntityIdAndTypeDto entityIdAndTypeDto,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC, size = 20) Pageable pageable) {
        Page<RootCommentWithReplyAndUserDto> comments = commentService.findRootCommentsWithFirstReply(pageable, entityIdAndTypeDto);
        return ResponseEntity.ok(comments);
    }

    @PreAuthorize("T(java.lang.Long).valueOf(principal) == #commentSaveDto.userId()")
    @PostMapping
    public ResponseEntity<CommentReadDto> createComment(@RequestBody @Validated CommentSaveDto commentSaveDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.createComment(commentSaveDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentReadDto> updateComment(@PathVariable("id") Long id,
                                                        @RequestBody @Validated HtmlContentDto htmlContent) {
        return ResponseEntity.ok(commentService.updateComment(id, htmlContent));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteComment(@PathVariable("id") Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/pin")
    public ResponseEntity<CommentReadDto> pinComment(@PathVariable("id") Long id,
                                                     @Validated EntityIdAndTypeDto entityIdAndTypeDto) {
        return ResponseEntity.ok(commentService.pinComment(id, entityIdAndTypeDto));
    }

    @PatchMapping("/{id}/unpin")
    public ResponseEntity<CommentReadDto> unpinComment(@PathVariable("id") Long id,
                                                       @Validated EntityIdAndTypeDto entityIdAndTypeDto) {
        return ResponseEntity.ok(commentService.unpinComment(id, entityIdAndTypeDto));
    }

}
