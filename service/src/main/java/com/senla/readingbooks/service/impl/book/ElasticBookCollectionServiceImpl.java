package com.senla.readingbooks.service.impl.book;

import com.senla.readingbooks.document.BookCollectionDocument;
import com.senla.readingbooks.dto.FullTextSearchDto;
import com.senla.readingbooks.dto.bookcollection.BookCollectionsFoundDto;
import com.senla.readingbooks.entity.collection.BookCollection;
import com.senla.readingbooks.mapper.ElasticBookCollectionMapper;
import com.senla.readingbooks.projection.book.BookCollectionProjection;
import com.senla.readingbooks.repository.elastic.ElasticBookCollectionRepository;
import com.senla.readingbooks.repository.jpa.book.BookCollectionRepository;
import com.senla.readingbooks.service.interfaces.book.ElasticBookCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ElasticBookCollectionServiceImpl implements ElasticBookCollectionService {
    private final ElasticBookCollectionRepository elasticBookCollectionRepository;
    private final BookCollectionRepository bookCollectionRepository;
    private final ElasticBookCollectionMapper elasticBookCollectionMapper;

    @Override
    public void saveBookCollectionDocument(BookCollection bookCollection) {
        BookCollectionDocument collectionDocument = elasticBookCollectionMapper.toDocument(bookCollection);
        elasticBookCollectionRepository.save(collectionDocument);
    }

    @Override
    public void deleteById(Long id) {
        elasticBookCollectionRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookCollectionsFoundDto> searchByTitleOrDescription(FullTextSearchDto searchDto) {
        PageRequest pageRequest = PageRequest.of(searchDto.pageNumber(), searchDto.pageSize());
        Page<BookCollectionDocument> collectionDocumentPage = elasticBookCollectionRepository.searchByTitleOrDescription(searchDto.query(), pageRequest);

        if (collectionDocumentPage.isEmpty()) {
            return Page.empty(pageRequest);
        }
        List<Long> collectionIds = collectionDocumentPage.getContent().stream()
                .map(BookCollectionDocument::getId)
                .toList();

        Map<Long, Integer> collectionIdOrderIndexMap = new HashMap<>();
        for (int i = 0; i < collectionIds.size(); i++) {
            collectionIdOrderIndexMap.put(collectionIds.get(i), i);
        }

        List<BookCollectionProjection> projections = bookCollectionRepository.findCollectionsWithAuthorsAndBooksCount(collectionIds);
        List<BookCollectionsFoundDto> result = projections.stream()
                .map(elasticBookCollectionMapper::mapToCollectionsFoundDto)
                .sorted(Comparator.comparingInt(projection -> collectionIdOrderIndexMap.get(projection.id())))
                .toList();

        return new PageImpl<>(result, pageRequest, collectionDocumentPage.getTotalElements());
    }

}
