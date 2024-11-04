package com.senla.readingbooks.service.interfaces.book;

import com.senla.readingbooks.dto.FullTextSearchDto;
import com.senla.readingbooks.dto.bookcollection.BookCollectionsFoundDto;
import com.senla.readingbooks.entity.collection.BookCollection;
import org.springframework.data.domain.Page;

public interface ElasticBookCollectionService {
    void saveBookCollectionDocument(BookCollection bookCollection);

    void deleteById(Long id);

    Page<BookCollectionsFoundDto> searchByTitleOrDescription(FullTextSearchDto searchDto);
}
