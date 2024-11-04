package com.senla.readingbooks.projection.book;

public interface BookCollectionProjection {
    Long getCollectionId();

    String getTitle();

    Integer getBooksCount();

    Long getAuthorId();

    String getUsername();

    String getAvatarUrl();
}
