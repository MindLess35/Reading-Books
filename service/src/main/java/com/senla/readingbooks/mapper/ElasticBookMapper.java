package com.senla.readingbooks.mapper;

import com.senla.readingbooks.document.BookDocument;
import com.senla.readingbooks.dto.book.BooksFoundDto;
import com.senla.readingbooks.dto.user.AuthorDto;
import com.senla.readingbooks.entity.book.Book;
import com.senla.readingbooks.entity.user.User;
import com.senla.readingbooks.projection.book.BookAuthorProjection;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ElasticBookMapper {

    BookDocument toDocument(Book book);

    default List<AuthorDto> mapUsersToAuthorsDocument(List<User> users) {
        if (users == null) {
            return Collections.emptyList();
        }
        return users.stream()
                .map(u -> new AuthorDto(u.getId(), u.getUsername()))
                .toList();
    }

    default BooksFoundDto mapToBooksFoundDto(BookAuthorProjection projection, Map<Long, List<AuthorDto>> bookIdAuthorsMap) {
        return new BooksFoundDto(
                projection.getBookId(),
                projection.getTitle(),
                projection.getCoverUrl(),
                bookIdAuthorsMap.get(projection.getBookId())
        );
    }

}
