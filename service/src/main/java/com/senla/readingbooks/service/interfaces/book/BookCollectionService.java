package com.senla.readingbooks.service.interfaces.book;

import com.senla.readingbooks.dto.BookCollectionFilterDto;
import com.senla.readingbooks.dto.BookCollectionsAsPageDto;
import com.senla.readingbooks.dto.bookcollection.AddBookToCollectionDto;
import com.senla.readingbooks.dto.bookcollection.BookCollectionReadDto;
import com.senla.readingbooks.dto.bookcollection.BookCollectionSaveDto;
import com.senla.readingbooks.entity.collection.BookCollection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookCollectionService {
    BookCollectionReadDto findBookCollectionById(Long id);

    BookCollection findById(Long id);

    BookCollectionReadDto createBookCollection(BookCollectionSaveDto dto);

    BookCollectionReadDto publishCollection(Long collectionId);

    void addBookToCollection(Long collectionId, Long bookId, AddBookToCollectionDto dto);

    BookCollectionReadDto updateBookCollection(Long id, BookCollectionSaveDto dto);

    void deleteBookCollection(Long id);

    boolean existsById(Long id);

    Page<BookCollectionsAsPageDto> findBookCollectionsAsPage(Pageable pageable, BookCollectionFilterDto filter);

    void checkAccessToCollection(Long collectionId);

    boolean isUserIsAuthorOfCollection(Long collectionId, Long userId);
}
