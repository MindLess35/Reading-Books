package com.senla.readingbooks.service.impl.book;

import com.senla.readingbooks.dto.bookcollection.AddBookToCollectionDto;
import com.senla.readingbooks.entity.book.Book;
import com.senla.readingbooks.entity.collection.BookCollection;
import com.senla.readingbooks.entity.collection.BookCollectionItem;
import com.senla.readingbooks.enums.book.PublicationStatus;
import com.senla.readingbooks.exception.base.BadRequestBaseException;
import com.senla.readingbooks.exception.base.ResourceNotFoundException;
import com.senla.readingbooks.repository.jpa.book.BookCollectionItemRepository;
import com.senla.readingbooks.repository.jpa.book.BookCollectionRepository;
import com.senla.readingbooks.service.interfaces.book.BookCollectionItemService;
import com.senla.readingbooks.service.interfaces.book.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.senla.readingbooks.property.CacheProperty.CACHE_SEPARATOR;
import static com.senla.readingbooks.service.impl.book.BookCollectionServiceImpl.CACHE_COLLECTION_ENTITY_ID;
import static com.senla.readingbooks.service.impl.book.BookCollectionServiceImpl.COLLECTION_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookCollectionItemServiceImpl implements BookCollectionItemService {
    private final BookCollectionItemRepository bookCollectionItemRepository;
    private final BookService bookService;
    private final BookCollectionRepository bookCollectionRepository;
    private final RedisTemplate<String, BookCollection> redisTemplate;

    @Override
    public void addBookToCollection(Long collectionId, Long bookId, AddBookToCollectionDto dto) {
        Book book = bookService.findByIdLazy(bookId);
        if (book.getStatus() == PublicationStatus.IS_DRAFT) {
            bookService.checkAccessToBook(bookId);
        }

        BookCollection collection = findBookCollectionById(collectionId);
        if (bookCollectionItemRepository.existsByBookCollectionIdAndBookId(collectionId, bookId)) {
            throw new BadRequestBaseException("Book with id [%d] is already in the collection with id [%d]".formatted(bookId, collectionId));
        }
        BookCollectionItem bookCollectionItem = BookCollectionItem.builder()
                .bookCollection(collection)
                .userDescription(dto.userDescription())
                .book(book)
                .build();
        bookCollectionItemRepository.save(bookCollectionItem);
    }

    private BookCollection findBookCollectionById(Long id) {
        String cacheKey = CACHE_COLLECTION_ENTITY_ID + CACHE_SEPARATOR + id;
        BookCollection cachedCollection = redisTemplate.opsForValue().get(cacheKey);
        if (cachedCollection != null) {
            return cachedCollection;
        }
        BookCollection collection = bookCollectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(COLLECTION_NOT_FOUND.formatted(id)));
        redisTemplate.opsForValue().set(cacheKey, collection);
        return collection;
    }

    @Override
    public boolean existsPublishedBookInCollection(Long collectionId, String status) {
        return bookCollectionItemRepository.existsPublishedBookInCollection(collectionId, status);
    }
}

