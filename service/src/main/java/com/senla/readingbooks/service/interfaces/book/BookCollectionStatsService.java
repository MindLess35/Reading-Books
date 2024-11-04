package com.senla.readingbooks.service.interfaces.book;

import com.senla.readingbooks.entity.collection.BookCollection;

public interface BookCollectionStatsService {
    void createBookCollectionStats(BookCollection bookCollection);

    void updateBookCollectionStats(Long bookCollectionId);

}
