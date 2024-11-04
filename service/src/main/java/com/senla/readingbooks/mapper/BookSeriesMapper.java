package com.senla.readingbooks.mapper;

import com.senla.readingbooks.dto.BookSeriesReadDto;
import com.senla.readingbooks.dto.BookSeriesSaveDto;
import com.senla.readingbooks.entity.book.BookSeries;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookSeriesMapper {

    BookSeries toEntity(BookSeriesSaveDto dto);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    BookSeriesReadDto toDto(BookSeries bookSeries);

    BookSeries updateEntity(BookSeriesSaveDto dto, @MappingTarget BookSeries bookSeries);

}
