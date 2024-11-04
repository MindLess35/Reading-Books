package com.senla.readingbooks.service.interfaces.book;

import com.senla.readingbooks.dto.BookSeriesReadDto;
import com.senla.readingbooks.dto.BookSeriesSaveDto;
import com.senla.readingbooks.entity.book.BookSeries;

public interface BookSeriesService {
    BookSeriesReadDto findBookSeriesById(Long id);

    BookSeriesReadDto createBookSeries(BookSeriesSaveDto dto);

    BookSeriesReadDto publishSeries(Long id);

    BookSeriesReadDto updateBookSeries(Long id, BookSeriesSaveDto dto);

    void deleteBookSeries(Long id);

    BookSeries findById(Long id);

    boolean existsById(Long id);

    void checkAccessToSeries(Long seriesId);

    boolean isUserIsAuthorOfSeries(Long seriesId, Long userId);
}
