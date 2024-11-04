package com.senla.readingbooks.dto.book;

import com.senla.readingbooks.enums.book.BookForm;
import com.senla.readingbooks.enums.book.Genre;
import com.senla.readingbooks.validation.group.OnCreate;
import com.senla.readingbooks.validation.group.OnUpdate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.List;

public record BookSaveDto(
        @NotBlank
        @Size(max = 64)
        String title,
        @NotNull
        BookForm form,
        @NotBlank
        @Size(max = 512)
        String annotation,
        @Size(max = 255)
        String authorNote,
        @Positive
        Long seriesId,
        @NotEmpty
        @UniqueElements
        @Size(max = 3)
        List<@NotNull Genre> genres,
        @Size(max = 10)
        @UniqueElements
        List<@NotBlank String> tags,
        @Null(groups = OnUpdate.class)
        @NotEmpty(groups = OnCreate.class)
        @Size(max = 3, groups = OnCreate.class)
        @UniqueElements(groups = OnCreate.class)
        List<@Positive(groups = OnCreate.class) @NotNull(groups = OnCreate.class) Long> authorIds) {
}

