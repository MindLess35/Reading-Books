package com.senla.readingbooks.mapper;

import com.senla.readingbooks.document.BookDocument;
import com.senla.readingbooks.dto.AuthorWithAvatarDto;
import com.senla.readingbooks.dto.book.BookReadDto;
import com.senla.readingbooks.dto.book.BookSaveDto;
import com.senla.readingbooks.entity.book.Book;
import com.senla.readingbooks.entity.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookMapper {

    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "genres", ignore = true)
    Book toEntity(BookSaveDto dto);

    BookDocument toDocument(Book book);

    Book updateEntity(BookSaveDto dto, @MappingTarget Book book);

    @Mapping(target = "authors", source = "users")
    @Mapping(target = "seriesId", source = "bookSeries.id")
    @Mapping(target = "seriesTitle", source = "bookSeries.title")
    BookReadDto toDto(Book book);

    default Set<AuthorWithAvatarDto> mapUsersToAuthors(List<User> users) {
        if (users == null) {
            return Collections.emptySet();
        }
        return users.stream()
                .map(u -> new AuthorWithAvatarDto(u.getId(), u.getUsername(), u.getAvatarUrl()))
                .collect(Collectors.toSet());
    }

}
