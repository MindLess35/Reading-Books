package com.senla.readingbooks.dto;

import java.io.Serializable;

public record AuthorWithAvatarDto(
        Long authorId,
        String username,
        String avatarUrl) implements Serializable {
}