package com.senla.readingbooks.dto.book;

import com.senla.readingbooks.dto.user.AuthorDto;

import java.util.List;

public record BooksFoundDto(
        Long id,
        String title,
        String coverUrl,
        List<AuthorDto> authors) {
}
