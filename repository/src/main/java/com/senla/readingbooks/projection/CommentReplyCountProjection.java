package com.senla.readingbooks.projection;

public interface CommentReplyCountProjection {
    Long getRootCommentId();

    Long getReplyCount();
}
