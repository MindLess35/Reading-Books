package com.senla.readingbooks.mapper;

import com.senla.readingbooks.dto.bookcollection.BookCollectionReadDto;
import com.senla.readingbooks.dto.bookcollection.BookCollectionSaveDto;
import com.senla.readingbooks.entity.collection.BookCollection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookCollectionMapper {

    @Mapping(target = "userId", source = "user.id")
    BookCollectionReadDto toDto(BookCollection bookCollection);

    BookCollection toEntity(BookCollectionSaveDto dto);

    BookCollection updateEntity(BookCollectionSaveDto dto, @MappingTarget BookCollection bookCollection);
}
