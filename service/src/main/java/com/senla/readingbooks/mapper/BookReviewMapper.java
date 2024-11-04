package com.senla.readingbooks.mapper;

import com.senla.readingbooks.dto.BookReviewReadDto;
import com.senla.readingbooks.dto.BookReviewSaveDto;
import com.senla.readingbooks.entity.book.BookReview;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookReviewMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "bookId", source = "book.id")
    BookReviewReadDto toDto(BookReview bookReview);

    BookReview toEntity(BookReviewSaveDto dto);

    BookReview updateEntity(BookReviewSaveDto dto, @MappingTarget BookReview bookReview);
}
