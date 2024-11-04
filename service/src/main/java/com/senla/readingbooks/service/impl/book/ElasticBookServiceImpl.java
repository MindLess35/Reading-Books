package com.senla.readingbooks.service.impl.book;

import com.senla.readingbooks.document.BookDocument;
import com.senla.readingbooks.dto.FullTextSearchDto;
import com.senla.readingbooks.dto.book.BooksFoundDto;
import com.senla.readingbooks.dto.user.AuthorDto;
import com.senla.readingbooks.entity.book.Book;
import com.senla.readingbooks.event.BookCreatedEvent;
import com.senla.readingbooks.mapper.ElasticBookMapper;
import com.senla.readingbooks.mapper.UserMapper;
import com.senla.readingbooks.projection.book.BookAuthorProjection;
import com.senla.readingbooks.repository.elastic.ElasticBookRepository;
import com.senla.readingbooks.repository.jpa.book.BookRepository;
import com.senla.readingbooks.service.interfaces.book.ElasticBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ElasticBookServiceImpl implements ElasticBookService {
    private final ElasticBookRepository elasticBookRepository;
    private final BookRepository bookRepository;
    private final ElasticBookMapper elasticBookMapper;
    private final UserMapper userMapper;

    @EventListener
    public void saveBookDocument(BookCreatedEvent event) {
        saveBookDocument(event.book());
    }

    @Override
    public void saveBookDocument(Book book) {
        BookDocument bookDocument = elasticBookMapper.toDocument(book);
        elasticBookRepository.save(bookDocument);
    }

    @Override
    public void deleteById(Long id) {
        elasticBookRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BooksFoundDto> searchByTitleOrAnnotation(FullTextSearchDto searchDto) {
        PageRequest pageRequest = PageRequest.of(searchDto.pageNumber(), searchDto.pageSize());
        Page<BookDocument> bookDocumentPage = elasticBookRepository.searchByTitleOrAnnotation(searchDto.query(), pageRequest);

        if (bookDocumentPage.isEmpty()) {
            return Page.empty(pageRequest);
        }
        List<Long> bookIds = bookDocumentPage.getContent().stream()
                .map(BookDocument::getId)
                .toList();

        Map<Long, Integer> bookIdOrderIndexMap = new HashMap<>();
        for (int i = 0; i < bookIds.size(); i++) {
            bookIdOrderIndexMap.put(bookIds.get(i), i);
        }

        List<BookAuthorProjection> bookAuthorProjections = bookRepository.findBooksWithAuthorsByIds(bookIds);
        Map<Long, List<AuthorDto>> bookIdAuthorsMap = buildBookIdAuthorsMap(bookAuthorProjections);

        List<BooksFoundDto> result = buildResult(bookAuthorProjections, bookIdAuthorsMap, bookIdOrderIndexMap);
        return new PageImpl<>(result, pageRequest, bookDocumentPage.getTotalElements());
    }

    private List<BooksFoundDto> buildResult(List<BookAuthorProjection> bookAuthorProjections,
                                            Map<Long, List<AuthorDto>> bookIdAuthorsMap,
                                            Map<Long, Integer> bookIdOrderIndexMap) {
        return bookAuthorProjections.stream()
                .collect(Collectors.toMap(
                        BookAuthorProjection::getBookId,
                        projection -> elasticBookMapper.mapToBooksFoundDto(projection, bookIdAuthorsMap),
                        (value, sameValue) -> value
                ))
                .values()
                .stream()
                .sorted(Comparator.comparingInt(bookDto -> bookIdOrderIndexMap.get(bookDto.id())))
                .toList();
    }

    private Map<Long, List<AuthorDto>> buildBookIdAuthorsMap(List<BookAuthorProjection> bookAuthorProjections) {
        Map<Long, List<AuthorDto>> bookIdAuthorsMap = new HashMap<>();
        for (BookAuthorProjection projection : bookAuthorProjections) {
            bookIdAuthorsMap
                    .computeIfAbsent(projection.getBookId(), key -> new ArrayList<>())
                    .add(userMapper.mapToAuthor(projection));
        }
        return bookIdAuthorsMap;
    }

}