package com.senla.readingbooks.mapper;

import com.senla.readingbooks.dto.AuthorWithAvatarDto;
import com.senla.readingbooks.dto.user.AuthorDto;
import com.senla.readingbooks.dto.user.UserCreateDto;
import com.senla.readingbooks.dto.user.UserReadDto;
import com.senla.readingbooks.dto.user.UserUpdateDto;
import com.senla.readingbooks.entity.user.User;
import com.senla.readingbooks.projection.book.BookAuthorProjection;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    User toEntity(UserCreateDto dto);

    User updateEntity(UserUpdateDto userUpdateDto, @MappingTarget User user);

    UserReadDto toDto(User user);

    AuthorWithAvatarDto mapToAuthorWithAvatar(BookAuthorProjection projection);

    AuthorDto mapToAuthor(BookAuthorProjection projection);

}
