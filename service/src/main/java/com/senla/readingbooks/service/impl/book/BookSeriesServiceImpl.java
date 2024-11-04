package com.senla.readingbooks.service.impl.book;

import com.senla.readingbooks.dto.BookSeriesReadDto;
import com.senla.readingbooks.dto.BookSeriesSaveDto;
import com.senla.readingbooks.entity.book.BookSeries;
import com.senla.readingbooks.entity.user.User;
import com.senla.readingbooks.enums.book.PublicationStatus;
import com.senla.readingbooks.enums.user.Role;
import com.senla.readingbooks.exception.base.BadRequestBaseException;
import com.senla.readingbooks.exception.base.ResourceNotFoundException;
import com.senla.readingbooks.mapper.BookSeriesMapper;
import com.senla.readingbooks.repository.jpa.book.BookSeriesRepository;
import com.senla.readingbooks.service.interfaces.book.BookSeriesService;
import com.senla.readingbooks.service.interfaces.user.UserService;
import com.senla.readingbooks.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.senla.readingbooks.property.CacheProperty.CACHE_SEPARATOR;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookSeriesServiceImpl implements BookSeriesService {
    private final BookSeriesRepository bookSeriesRepository;
    private final BookSeriesMapper bookSeriesMapper;
    private final UserService userService;
    private final RedisTemplate<String, BookSeriesReadDto> redisTemplate;
    private static final String SERIES_NOT_FOUND = "Book series with id [%d] not found";
    private static final String CACHE_SERIES_ENTITY_ID = "series::entity::id";
    private static final String CACHE_SERIES_DTO_ID = "series::dto::id";
    private static final String CACHE_SERIES_EXISTS_ID = "series::exists::id";

    @Override
    public BookSeriesReadDto findBookSeriesById(Long id) {
        String cacheKey = CACHE_SERIES_DTO_ID + CACHE_SEPARATOR + id;
        BookSeriesReadDto bookSeriesReadDto = redisTemplate.opsForValue().get(cacheKey);
        if (bookSeriesReadDto == null) {
            bookSeriesReadDto = bookSeriesRepository.findById(id)
                    .map(bookSeriesMapper::toDto)
                    .orElseThrow(() -> new ResourceNotFoundException(SERIES_NOT_FOUND.formatted(id)));

            redisTemplate.opsForValue().set(cacheKey, bookSeriesReadDto);
        }

        if (bookSeriesReadDto.status() == PublicationStatus.IS_DRAFT) {
            if (AuthUtil.isUserAlreadyAuthenticated()) {
                checkAccessToSeries(id);
            } else {
                throw new AccessDeniedException("Book series with id [%d] is a draft only its author can access".formatted(id));
            }
        }
        return bookSeriesReadDto;
    }

    @Override
    @Cacheable(value = CACHE_SERIES_ENTITY_ID, key = "#id")
    public BookSeries findById(Long id) {
        return bookSeriesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(SERIES_NOT_FOUND.formatted(id)));
    }

    @Override
    @Cacheable(value = CACHE_SERIES_EXISTS_ID, key = "#id")
    public boolean existsById(Long id) {
        return bookSeriesRepository.existsById(id);
    }

    @Override
    @Transactional
    @CachePut(value = CACHE_SERIES_DTO_ID, key = "#result.id")
    public BookSeriesReadDto createBookSeries(BookSeriesSaveDto dto) {
        User user = userService.findById(dto.userId());
        BookSeries bookSeries = bookSeriesMapper.toEntity(dto);
        bookSeries.setUser(user);
        return bookSeriesMapper.toDto(bookSeriesRepository.save(bookSeries));
    }

    @Override
    @Transactional
    @CachePut(value = CACHE_SERIES_DTO_ID, key = "#result.id")
    public BookSeriesReadDto publishSeries(Long id) {
        if (!bookSeriesRepository.existsPublishedBookInSeries(id, PublicationStatus.IS_DRAFT.name())) {
            throw new BadRequestBaseException("Book series must contain at least one published book in order to be published");
        }
        BookSeries series = bookSeriesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(SERIES_NOT_FOUND.formatted(id)));
        if (series.getStatus() != PublicationStatus.IS_DRAFT) {
            throw new BadRequestBaseException("Series with id [%d] is already published".formatted(id));
        }
        series.setStatus(PublicationStatus.IN_PROGRESS);
        BookSeries updatedSeries = bookSeriesRepository.save(series);
        return bookSeriesMapper.toDto(updatedSeries);
    }

    @Override
    @Transactional
    @CachePut(value = CACHE_SERIES_DTO_ID, key = "#id")
    public BookSeriesReadDto updateBookSeries(Long id, BookSeriesSaveDto dto) {
        return bookSeriesRepository.findById(id)
                .map(series -> bookSeriesMapper.updateEntity(dto, series))
                .map(bookSeriesRepository::saveAndFlush)
                .map(bookSeriesMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException(SERIES_NOT_FOUND.formatted(id)));
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CACHE_SERIES_EXISTS_ID, key = "#id"),
            @CacheEvict(value = CACHE_SERIES_DTO_ID, key = "#id"),
            @CacheEvict(value = CACHE_SERIES_ENTITY_ID, key = "#id")})
    public void deleteBookSeries(Long id) {
        bookSeriesRepository.delete(bookSeriesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(SERIES_NOT_FOUND.formatted(id))));
    }

    @Override
    public void checkAccessToSeries(Long seriesId) {
        Role role = AuthUtil.getAuthenticatedUserRole();
        if (role == Role.MODERATOR)
            return;
        Long authorizedUserId = AuthUtil.getAuthenticatedUserId();
        if (!isUserIsAuthorOfSeries(seriesId, authorizedUserId))
            throw new AccessDeniedException("Unauthorized access to a book series with id [%d] by a user with id [%d] and the [%s] role. Or the series is a draft. Only its author can access the draft."
                    .formatted(seriesId, authorizedUserId, role));
    }

    @Override
    public boolean isUserIsAuthorOfSeries(Long seriesId, Long userId) {
        return bookSeriesRepository.userIsAuthorOfSeries(seriesId, userId);
    }
}
