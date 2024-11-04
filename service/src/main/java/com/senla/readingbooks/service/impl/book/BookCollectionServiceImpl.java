package com.senla.readingbooks.service.impl.book;

import com.senla.readingbooks.dto.BookCollectionFilterDto;
import com.senla.readingbooks.dto.BookCollectionsAsPageDto;
import com.senla.readingbooks.dto.bookcollection.AddBookToCollectionDto;
import com.senla.readingbooks.dto.bookcollection.BookCollectionReadDto;
import com.senla.readingbooks.dto.bookcollection.BookCollectionSaveDto;
import com.senla.readingbooks.entity.collection.BookCollection;
import com.senla.readingbooks.entity.user.User;
import com.senla.readingbooks.enums.book.PublicationStatus;
import com.senla.readingbooks.enums.user.Role;
import com.senla.readingbooks.exception.base.BadRequestBaseException;
import com.senla.readingbooks.exception.base.ResourceNotFoundException;
import com.senla.readingbooks.mapper.BookCollectionMapper;
import com.senla.readingbooks.repository.jpa.book.BookCollectionRepository;
import com.senla.readingbooks.service.interfaces.book.BookCollectionItemService;
import com.senla.readingbooks.service.interfaces.book.BookCollectionService;
import com.senla.readingbooks.service.interfaces.book.BookCollectionStatsService;
import com.senla.readingbooks.service.interfaces.book.BookService;
import com.senla.readingbooks.service.interfaces.book.ElasticBookCollectionService;
import com.senla.readingbooks.service.interfaces.user.UserService;
import com.senla.readingbooks.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.senla.readingbooks.property.CacheProperty.CACHE_SEPARATOR;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookCollectionServiceImpl implements BookCollectionService {
    private final BookCollectionRepository bookCollectionRepository;
    private final UserService userService;
    private final BookCollectionStatsService bookCollectionStatsService;
    private final BookCollectionItemService bookCollectionItemService;
    private final BookCollectionMapper bookCollectionMapper;
    private final ElasticBookCollectionService elasticBookCollectionService;
    private final RedisTemplate<String, BookCollectionReadDto> redisTemplate;
    public static final String COLLECTION_NOT_FOUND = "Book Collection with id [%d] not found";
    private static final String CACHE_COLLECTION_DTO_ID = "collection::dto::id";
    private static final String CACHE_COLLECTION_EXISTS_ID = "collection::exists::id";
    public static final String CACHE_COLLECTION_ENTITY_ID = "collection::entity::id";

    @Override
    public BookCollectionReadDto findBookCollectionById(Long id) {
        String cacheKey = CACHE_COLLECTION_DTO_ID + CACHE_SEPARATOR + id;
        BookCollectionReadDto bookCollectionReadDto = redisTemplate.opsForValue().get(cacheKey);
        if (bookCollectionReadDto == null) {
            bookCollectionReadDto = bookCollectionRepository.findById(id)
                    .map(bookCollectionMapper::toDto)
                    .orElseThrow(() -> new ResourceNotFoundException(COLLECTION_NOT_FOUND.formatted(id)));

            redisTemplate.opsForValue().set(cacheKey, bookCollectionReadDto);
        }

        if (bookCollectionReadDto.isDraft()) {
            if (AuthUtil.isUserAlreadyAuthenticated()) {
                checkAccessToCollection(id);
            } else {
                throw new AccessDeniedException("Book collection with id [%d] is a draft only its author can access".formatted(id));
            }
        }
        return bookCollectionReadDto;
    }

    @Override
    @Cacheable(value = CACHE_COLLECTION_ENTITY_ID, key = "#id")
    public BookCollection findById(Long id) {
        return bookCollectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(COLLECTION_NOT_FOUND.formatted(id)));
    }

    @Override
    public Page<BookCollectionsAsPageDto> findBookCollectionsAsPage(Pageable pageable, BookCollectionFilterDto filter) {
        return bookCollectionRepository.findBookCollectionsAsPage(pageable, filter);
    }

    @Override
    @Transactional
    @CachePut(value = CACHE_COLLECTION_DTO_ID, key = "#result.id")
    public BookCollectionReadDto createBookCollection(BookCollectionSaveDto dto) {
        User user = userService.findById(dto.userId());
        BookCollection bookCollection = bookCollectionMapper.toEntity(dto);
        bookCollection.setUser(user);
        BookCollection savedCollection = bookCollectionRepository.save(bookCollection);

        bookCollectionStatsService.createBookCollectionStats(savedCollection);
        elasticBookCollectionService.saveBookCollectionDocument(savedCollection);
        return bookCollectionMapper.toDto(savedCollection);
    }

    @Override
    @Transactional
    @CachePut(value = CACHE_COLLECTION_DTO_ID, key = "#result.id")
    public BookCollectionReadDto publishCollection(Long id) {
        if (!bookCollectionItemService.existsPublishedBookInCollection(id, PublicationStatus.IS_DRAFT.name())) {
            throw new BadRequestBaseException("Book collection must contain at least one published book in order to be published");
        }
        BookCollection collection = bookCollectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(COLLECTION_NOT_FOUND.formatted(id)));
        if (!collection.getIsDraft()) {
            throw new BadRequestBaseException("Collection with id [%d] is already published".formatted(id));
        }
        collection.setIsDraft(false);
        BookCollection updatedCollection = bookCollectionRepository.save(collection);
        return bookCollectionMapper.toDto(updatedCollection);
    }

    @Transactional
    public void addBookToCollection(Long collectionId, Long bookId, AddBookToCollectionDto dto) {
        bookCollectionItemService.addBookToCollection(collectionId, bookId, dto);
    }

    @Override
    @Transactional
    @CachePut(value = CACHE_COLLECTION_DTO_ID, key = "#id")
    public BookCollectionReadDto updateBookCollection(Long id, BookCollectionSaveDto dto) {
        BookCollection updatedBookCollection = bookCollectionRepository.findById(id)
                .map(bc -> bookCollectionMapper.updateEntity(dto, bc))
                .map(bookCollectionRepository::save)
                .orElseThrow(() -> new ResourceNotFoundException(COLLECTION_NOT_FOUND.formatted(id)));

        bookCollectionStatsService.updateBookCollectionStats(id);
        elasticBookCollectionService.saveBookCollectionDocument(updatedBookCollection);
        return bookCollectionMapper.toDto(updatedBookCollection);
    }

    @Override
    @Cacheable(value = CACHE_COLLECTION_EXISTS_ID, key = "#id")
    public boolean existsById(Long id) {
        return bookCollectionRepository.existsById(id);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CACHE_COLLECTION_DTO_ID, key = "#id"),
            @CacheEvict(value = CACHE_COLLECTION_EXISTS_ID, key = "#id")})
    public void deleteBookCollection(Long id) {
        bookCollectionRepository.delete(bookCollectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(COLLECTION_NOT_FOUND.formatted(id))));
        elasticBookCollectionService.deleteById(id);
    }

    @Override
    public void checkAccessToCollection(Long collectionId) {
        Role role = AuthUtil.getAuthenticatedUserRole();
        if (role == Role.MODERATOR)
            return;

        Long authorizedUserId = AuthUtil.getAuthenticatedUserId();
        if (!isUserIsAuthorOfCollection(collectionId, authorizedUserId))
            throw new AccessDeniedException("Unauthorized access to a book collection with id [%d] by a user with id [%d] and the [%s] role. Or the collection is a draft. Only its author can access the draft."
                    .formatted(collectionId, authorizedUserId, role));
    }

    @Override
    public boolean isUserIsAuthorOfCollection(Long collectionId, Long userId) {
        return bookCollectionRepository.userIsAuthorOfCollection(collectionId, userId);
    }
}
