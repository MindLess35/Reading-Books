package com.senla.readingbooks.dto.comment;

import com.senla.readingbooks.dto.AuthorWithAvatarDto;
import com.senla.readingbooks.enums.EntityType;

import java.time.Instant;

public record RootCommentWithReplyAndUserDto(
        Long id,
        Long entityId,
        EntityType entityType,
        Integer likesCount,
        Integer dislikesCount,
        Boolean isPinned,
        Instant createdAt,
        Instant updatedAt,
        String contentUrl,
        Long otherRepliesCount,
        AuthorWithAvatarDto author,
        ReplyCommentWithUserDto firstReply) {
}

