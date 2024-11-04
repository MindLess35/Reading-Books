package com.senla.readingbooks.projection.book;

public interface BookAuthorProjection {
    Long getBookId();

    String getTitle();

    String getCoverUrl();

    Long getAuthorId();

    String getUsername();

    String getAvatarUrl();
}

