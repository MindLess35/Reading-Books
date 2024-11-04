package com.senla.readingbooks.mapper;

import com.senla.readingbooks.document.BookCollectionDocument;
import com.senla.readingbooks.dto.bookcollection.BookCollectionsFoundDto;
import com.senla.readingbooks.entity.collection.BookCollection;
import com.senla.readingbooks.projection.book.BookCollectionProjection;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ElasticBookCollectionMapper {

    BookCollectionDocument toDocument(BookCollection bookCollection);

    default BookCollectionsFoundDto mapToCollectionsFoundDto(BookCollectionProjection projection) {
        return new BookCollectionsFoundDto(
                projection.getCollectionId(),
                projection.getTitle(),
                projection.getBooksCount(),
                projection.getAuthorId(),
                projection.getUsername(),
                projection.getAvatarUrl()
        );
    }
}
