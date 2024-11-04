package com.senla.readingbooks.projection;

import com.senla.readingbooks.enums.EntityType;

import java.time.Instant;

public interface CommentWithFirstReplyProjection {
    Long getId();

    Long getUserId();

    Long getEntityId();

    EntityType getEntityType();

    Long getParentId();

    Integer getLikesCount();

    Integer getDislikesCount();

    Boolean getIsPinned();

    Instant getCreatedAt();

    Instant getUpdatedAt();

    String getContentUrl();

    Long getReplyId();

    Long getReplyUserId();

    Long getReplyEntityId();

    EntityType getReplyEntityType();

    Long getReplyParentId();

    Integer getReplyLikesCount();

    Integer getReplyDislikesCount();

    Boolean getReplyIsPinned();

    Instant getReplyCreatedAt();

    Instant getReplyUpdatedAt();

    String getReplyContentUrl();
}

