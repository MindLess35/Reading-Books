package com.senla.readingbooks.projection;

public interface UserWithBooksCountProjection {

    Long getId();

    String getUsername();

    String getAvatarUrl();

    Integer getBooksWrittenCount();
}

