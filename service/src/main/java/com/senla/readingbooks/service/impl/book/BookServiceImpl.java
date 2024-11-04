package com.senla.readingbooks.service.impl.book;

import com.senla.readingbooks.dto.AuthorWithAvatarDto;
import com.senla.readingbooks.dto.BookFilterDto;
import com.senla.readingbooks.dto.BooksAsPageDto;
import com.senla.readingbooks.dto.book.BookReadDto;
import com.senla.readingbooks.dto.book.BookSaveDto;
import com.senla.readingbooks.entity.book.Book;
import com.senla.readingbooks.entity.book.BookSeries;
import com.senla.readingbooks.entity.user.User;
import com.senla.readingbooks.enums.book.Genre;
import com.senla.readingbooks.enums.book.PublicationStatus;
import com.senla.readingbooks.enums.user.Role;
import com.senla.readingbooks.event.BookCreatedEvent;
import com.senla.readingbooks.exception.base.BadRequestBaseException;
import com.senla.readingbooks.exception.base.ResourceNotFoundException;
import com.senla.readingbooks.mapper.BookMapper;
import com.senla.readingbooks.mapper.UserMapper;
import com.senla.readingbooks.projection.book.BookAuthorProjection;
import com.senla.readingbooks.projection.book.BookGenreProjection;
import com.senla.readingbooks.repository.jpa.book.BookRepository;
import com.senla.readingbooks.service.interfaces.book.BookSeriesService;
import com.senla.readingbooks.service.interfaces.book.BookService;
import com.senla.readingbooks.service.interfaces.book.BookStatsService;
import com.senla.readingbooks.service.interfaces.book.ElasticBookService;
import com.senla.readingbooks.service.interfaces.user.UserService;
import com.senla.readingbooks.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.senla.readingbooks.property.CacheProperty.CACHE_SEPARATOR;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final UserService userService;
    private final BookMapper bookMapper;
    private final UserMapper userMapper;
    private final BookStatsService bookStatsService;
    private final ElasticBookService elasticBookService;
    private final BookSeriesService bookSeriesService;
    private final ApplicationEventPublisher eventPublisher;
    private final RedisTemplate<String, BookReadDto> redisTemplate;
    private static final String BOOK_NOT_FOUND = "Book with id [%d] not found";
    private static final String CACHE_BOOK_EXISTS_ID = "book::exists::id";
    private static final String CACHE_BOOK_LAZY_ID = "book::lazy::id";
    private static final String CACHE_BOOK_DTO_ID = "book::dto::id";

    @Override
    public BookReadDto findByIdEager(Long id) {
        String cacheKey = CACHE_BOOK_DTO_ID + CACHE_SEPARATOR + id;
        BookReadDto bookReadDto = redisTemplate.opsForValue().get(cacheKey);
        if (bookReadDto == null) {
            bookReadDto = bookRepository.findBookByIdEager(id)
                    .map(bookMapper::toDto)
                    .orElseThrow(() -> new ResourceNotFoundException(BOOK_NOT_FOUND.formatted(id)));

            redisTemplate.opsForValue().set(cacheKey, bookReadDto);
        }

        if (bookReadDto.getStatus() == PublicationStatus.IS_DRAFT) {
            if (AuthUtil.isUserAlreadyAuthenticated()) {
                checkAccessToBook(id);
            } else {
                throw new AccessDeniedException("Book with id [%d] is a draft only its author can access".formatted(id));
            }
        }
        return bookReadDto;
    }

    @Override
    @Cacheable(value = CACHE_BOOK_LAZY_ID, key = "#id")
    public Book findByIdLazy(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(BOOK_NOT_FOUND.formatted(id)));
    }

    @Override
    @Cacheable(value = CACHE_BOOK_EXISTS_ID, key = "#id")
    public boolean existsById(Long id) {
        return bookRepository.existsById(id);
    }

    @Override
    public Page<BooksAsPageDto> findBooksAsPage(Pageable pageable, BookFilterDto filter) {
        Page<BooksAsPageDto> page = bookRepository.findBooksAsPage(pageable, filter);
        if (page.isEmpty()) {
            return page;
        }

        List<BooksAsPageDto> booksAsPage = page.getContent();
        List<Long> bookIds = booksAsPage.stream()
                .map(BooksAsPageDto::getId)
                .toList();

        List<BookGenreProjection> retrievedGenres = bookRepository.findGenresByBookIds(bookIds);
        List<BookAuthorProjection> retrievedAuthors = bookRepository.findAuthorsByBookIds(bookIds);

        for (BooksAsPageDto dto : booksAsPage) {
            Long bookId = dto.getId();

            List<Genre> genres = retrievedGenres.stream()
                    .filter(row -> row.getBookId().equals(bookId))
                    .map(BookGenreProjection::getGenre)
                    .toList();
            dto.setGenres(genres);

            List<AuthorWithAvatarDto> authorsList = retrievedAuthors.stream()
                    .filter(row -> row.getBookId().equals(bookId))
                    .map(userMapper::mapToAuthorWithAvatar)
                    .toList();
            dto.setAuthors(authorsList);
        }

        return page;
    }

    @Override
    @Transactional
    @CachePut(value = CACHE_BOOK_DTO_ID, key = "#result.id")
    public BookReadDto createBook(BookSaveDto dto) {
        Optional<Long> seriesIdOptional = Optional.ofNullable(dto.seriesId());
        seriesIdOptional.ifPresent(bookSeriesService::checkAccessToSeries);
        BookSeries bookSeries = seriesIdOptional
                .map(bookSeriesService::findById)
                .orElse(null);

        List<User> users;
        List<Long> authorIds = dto.authorIds();
        if (authorIds.size() == 1) {
            users = new ArrayList<>(List.of(userService.findById(authorIds.get(0))));
        } else
            users = userService.findUsersByIds(authorIds);

        if (users.size() != authorIds.size()) {
            authorIds.removeAll(users.stream().map(User::getId).toList());
            throw new ResourceNotFoundException("User(s) with the specified id(s) " + authorIds + " not found");
        }

        Book book = bookMapper.toEntity(dto);
        book.setBookSeries(bookSeries);
        Book savedBook = bookRepository.save(book);

        BookReadDto resultDto = bookMapper.toDto(savedBook);
        resultDto.setAuthors(bookMapper.mapUsersToAuthors(users));
        List<Genre> genres = dto.genres();
        List<String> tags = dto.tags();
        resultDto.setGenres(new HashSet<>(genres));
        resultDto.setTags(new HashSet<>(tags));

        Long bookId = savedBook.getId();
        bookRepository.saveBookAuthorsInBatch(bookId, authorIds);
        bookRepository.saveGenresInBatch(bookId, genres);
        bookRepository.saveTagsInBatch(bookId, tags);
        eventPublisher.publishEvent(new BookCreatedEvent(savedBook, users));
        return resultDto;
    }

    @Override
    @Transactional
    @CachePut(value = CACHE_BOOK_DTO_ID, key = "#id")
    public BookReadDto publishBook(Long id) {
        if (!bookRepository.existsPublishedChapterById(id)) {
            throw new BadRequestBaseException("Book must contain at least one published chapter");
        }
        Book book = bookRepository.findBookByIdEager(id)
                .orElseThrow(() -> new ResourceNotFoundException(BOOK_NOT_FOUND.formatted(id)));
        if (book.getStatus() != PublicationStatus.IS_DRAFT) {
            throw new BadRequestBaseException("Book with id [%d] is already published".formatted(id));
        }
        book.setStatus(PublicationStatus.IN_PROGRESS);
        bookRepository.save(book);
        bookStatsService.updatePublicationDate(id);
        return bookMapper.toDto(book);
    }

    @Override
    @Transactional
    @CachePut(value = CACHE_BOOK_DTO_ID, key = "#result.id")
    public BookReadDto updateBook(Long id, BookSaveDto dto) {
        Book book = bookRepository.findBookByIdEager(id)
                .orElseThrow(() -> new ResourceNotFoundException(BOOK_NOT_FOUND.formatted(id)));

        Long seriesId = dto.seriesId();
        BookSeries currentSeries = book.getBookSeries();
        if (seriesId != null) {
            if (currentSeries == null || !Objects.equals(currentSeries.getId(), seriesId)) {
                bookSeriesService.checkAccessToSeries(seriesId);
                BookSeries newSeries = bookSeriesService.findById(seriesId);
                book.setBookSeries(newSeries);
            }
        } else if (currentSeries != null) {
            book.setBookSeries(null);
        }
        Book updatedBook = bookMapper.updateEntity(dto, book);
        bookRepository.save(updatedBook);
        bookStatsService.updateBookStatistics(id);

        if (!updatedBook.getTitle().equals(book.getTitle()) || !updatedBook.getAnnotation().equals(book.getAnnotation())) {
            elasticBookService.saveBookDocument(updatedBook);
        }
        return bookMapper.toDto(updatedBook);
    }


    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CACHE_BOOK_DTO_ID, key = "#id"),
            @CacheEvict(value = CACHE_BOOK_EXISTS_ID, key = "#id"),
            @CacheEvict(value = CACHE_BOOK_LAZY_ID, key = "#id")})
    public void deleteBook(Long id) {
        bookRepository.delete(bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(BOOK_NOT_FOUND.formatted(id))));
        elasticBookService.deleteById(id);
    }

    @Override
    public void checkAccessToBook(Long bookId) {
        Role role = AuthUtil.getAuthenticatedUserRole();
        if (role == Role.MODERATOR)
            return;

        Long authorizedUserId = AuthUtil.getAuthenticatedUserId();
        if (!isUserIsAuthorOfBook(bookId, authorizedUserId))
            throw new AccessDeniedException("Unauthorized access to a book with id [%d] by a user with id [%d] and the [%s] role. Or the book is a draft. Only its author can access the draft."
                    .formatted(bookId, authorizedUserId, role));
    }

    @Override
    public boolean isUserIsAuthorOfBook(Long bookId, Long userId) {
        return bookRepository.userIsAuthorOfBook(bookId, userId);
    }
}
