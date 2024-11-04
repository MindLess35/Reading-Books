package com.senla.readingbooks.dto.comment;

import com.senla.readingbooks.enums.EntityType;

import java.io.Serializable;
import java.time.Instant;

public record CommentReadDto(
        Long id,
        Long entityId,
        EntityType entityType,
        Long parentId,
        Long userId,
        Integer likesCount,
        Integer dislikesCount,
        Boolean isPinned,
        Instant createdAt,
        Instant updatedAt,
        String contentUrl) implements Serializable {
}