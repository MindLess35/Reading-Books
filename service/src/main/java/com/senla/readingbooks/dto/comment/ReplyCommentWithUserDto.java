package com.senla.readingbooks.dto.comment;

import com.senla.readingbooks.dto.AuthorWithAvatarDto;

import java.time.Instant;

public record ReplyCommentWithUserDto(
        Long id,
        Integer likesCount,
        Integer dislikesCount,
        Instant createdAt,
        Instant updatedAt,
        String contentUrl,
        AuthorWithAvatarDto author) {
}