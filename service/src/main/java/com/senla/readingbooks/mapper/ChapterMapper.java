package com.senla.readingbooks.mapper;

import com.senla.readingbooks.dto.ChapterReadDto;
import com.senla.readingbooks.dto.ChapterSaveDto;
import com.senla.readingbooks.entity.book.Chapter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ChapterMapper {

    Chapter toEntity(ChapterSaveDto dto);

    @Mapping(target = "bookId", source = "book.id")
    ChapterReadDto toDto(Chapter dto);

    Chapter updateEntity(ChapterSaveDto chapterSaveDto, @MappingTarget Chapter chapter);

}
