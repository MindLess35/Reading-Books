package com.senla.readingbooks.service.interfaces.book;

import com.senla.readingbooks.dto.bookcollection.AddBookToCollectionDto;

public interface BookCollectionItemService {
    void addBookToCollection(Long collectionId, Long bookId, AddBookToCollectionDto dto);

    boolean existsPublishedBookInCollection(Long collectionId, String status);
}
