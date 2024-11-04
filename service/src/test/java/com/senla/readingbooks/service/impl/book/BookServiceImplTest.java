package com.senla.readingbooks.service.impl.book;


import com.senla.readingbooks.dto.AuthorWithAvatarDto;
import com.senla.readingbooks.dto.book.BookReadDto;
import com.senla.readingbooks.dto.book.BookSaveDto;
import com.senla.readingbooks.entity.book.Book;
import com.senla.readingbooks.entity.book.BookSeries;
import com.senla.readingbooks.entity.user.User;
import com.senla.readingbooks.enums.book.AccessType;
import com.senla.readingbooks.enums.book.BookForm;
import com.senla.readingbooks.enums.book.Genre;
import com.senla.readingbooks.enums.book.PublicationStatus;
import com.senla.readingbooks.enums.user.Role;
import com.senla.readingbooks.event.BookCreatedEvent;
import com.senla.readingbooks.exception.base.BadRequestBaseException;
import com.senla.readingbooks.exception.base.ResourceNotFoundException;
import com.senla.readingbooks.mapper.BookMapper;
import com.senla.readingbooks.repository.jpa.book.BookRepository;
import com.senla.readingbooks.service.interfaces.book.BookSeriesService;
import com.senla.readingbooks.service.interfaces.book.BookStatsService;
import com.senla.readingbooks.service.interfaces.book.ElasticBookService;
import com.senla.readingbooks.service.interfaces.user.UserService;
import com.senla.readingbooks.util.AuthUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {
    @Mock
    private BookRepository bookRepository;
    @Mock
    private UserService userService;
    @Mock
    private BookMapper bookMapper;
    @Mock
    private BookStatsService bookStatsService;
    @Mock
    private ElasticBookService elasticBookService;
    @Mock
    private BookSeriesService bookSeriesService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private RedisTemplate<String, BookReadDto> redisTemplate;
    @Mock
    private ValueOperations<String, BookReadDto> valueOperations;
    @InjectMocks
    private BookServiceImpl bookService;
    private static final Long BOOK_ID = 1L;
    private static final Long SERIES_ID = 3L;
    private static final String BOOK_NOT_FOUND = "Book with id [%d] not found";
    private static final Long USER_ID = 100L;


    @Test
    void findByIdEager_shouldReturnBookReadDto_WhenFindByIdEagerWithAccess() {
        Book book = createBook(PublicationStatus.COMPLETED);
        BookReadDto bookReadDto = createBookReadDto(PublicationStatus.COMPLETED);

        try (MockedStatic<AuthUtil> authUtilMockedStatic = mockStatic(AuthUtil.class)) {
            authUtilMockedStatic.when(AuthUtil::getAuthenticatedUserRole).thenReturn(Role.AUTHOR);
            authUtilMockedStatic.when(AuthUtil::getAuthenticatedUserId).thenReturn(USER_ID);

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(bookRepository.findBookByIdEager(BOOK_ID)).thenReturn(Optional.of(book));
            when(bookMapper.toDto(book)).thenReturn(bookReadDto);

            BookReadDto result = bookService.findByIdEager(BOOK_ID);

            assertEquals(result, bookReadDto);
            verify(bookRepository).findBookByIdEager(BOOK_ID);
            verify(bookMapper).toDto(book);
            verifyNoMoreInteractions(bookRepository, bookMapper);
        }
    }

    @Test
    void findByIdEager_shouldThrowAccessDeniedException_WhenAccessToDraftIsUnauthorized() {
        Book book = createBook(PublicationStatus.IS_DRAFT);
        when(bookRepository.findBookByIdEager(BOOK_ID)).thenReturn(Optional.of(book));
        when(bookMapper.toDto(book)).thenReturn(createBookReadDto(PublicationStatus.IS_DRAFT));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        try (MockedStatic<AuthUtil> authUtilMockedStatic = mockStatic(AuthUtil.class)) {
            authUtilMockedStatic.when(AuthUtil::getAuthenticatedUserRole).thenReturn(Role.READER);
            authUtilMockedStatic.when(AuthUtil::getAuthenticatedUserId).thenReturn(USER_ID);

            assertThatThrownBy(() -> bookService.findByIdEager(BOOK_ID))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Book with id [%d] is a draft only its author can access".formatted(BOOK_ID));

            verify(bookRepository).findBookByIdEager(BOOK_ID);
            verifyNoMoreInteractions(bookRepository);
        }
    }

    @Test
    void findByIdEager_shouldThrowResourceNotFoundException_WhenBookNotFound() {
        when(bookRepository.findBookByIdEager(BOOK_ID)).thenReturn(Optional.empty());
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        try (MockedStatic<AuthUtil> authUtilMockedStatic = mockStatic(AuthUtil.class)) {
            authUtilMockedStatic.when(AuthUtil::getAuthenticatedUserRole).thenReturn(Role.AUTHOR);
            authUtilMockedStatic.when(AuthUtil::getAuthenticatedUserId).thenReturn(USER_ID);

            assertThatThrownBy(() -> bookService.findByIdEager(BOOK_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(BOOK_NOT_FOUND.formatted(BOOK_ID));

            verify(bookRepository).findBookByIdEager(BOOK_ID);
            verifyNoInteractions(bookMapper);
            verifyNoMoreInteractions(bookRepository);
        }
    }

    @Test
    void existsById_shouldReturnTrue_WhenBookExistsById() {
        when(bookRepository.existsById(BOOK_ID)).thenReturn(true);

        boolean exists = bookService.existsById(BOOK_ID);

        assertTrue(exists);
        verify(bookRepository).existsById(BOOK_ID);
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    void findByIdLazy_shouldReturnBook_WhenFindByIdLazy() {
        Book book = createBook(PublicationStatus.COMPLETED);
        when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.of(book));

        Book result = bookService.findByIdLazy(BOOK_ID);

        assertEquals(result, book);
        verify(bookRepository).findById(BOOK_ID);
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    void checkAccessToBook_shouldThrowAccessDeniedException_WhenUserIsNotAuthor() {
        try (MockedStatic<AuthUtil> authUtilMockedStatic = mockStatic(AuthUtil.class)) {
            authUtilMockedStatic.when(AuthUtil::getAuthenticatedUserRole).thenReturn(Role.AUTHOR);
            authUtilMockedStatic.when(AuthUtil::getAuthenticatedUserId).thenReturn(USER_ID);
            when(bookRepository.userIsAuthorOfBook(BOOK_ID, USER_ID)).thenReturn(false);

            assertThatThrownBy(() -> bookService.checkAccessToBook(BOOK_ID))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Unauthorized access to a book with id");

            verify(bookRepository).userIsAuthorOfBook(BOOK_ID, USER_ID);
            verifyNoMoreInteractions(bookRepository);
        }
    }

    @Test
    void checkAccessToBook_shouldNotThrow_WhenUserIsAuthor() {
        try (MockedStatic<AuthUtil> authUtilMockedStatic = mockStatic(AuthUtil.class)) {
            authUtilMockedStatic.when(AuthUtil::getAuthenticatedUserRole).thenReturn(Role.AUTHOR);
            authUtilMockedStatic.when(AuthUtil::getAuthenticatedUserId).thenReturn(USER_ID);
            when(bookRepository.userIsAuthorOfBook(BOOK_ID, USER_ID)).thenReturn(true);

            bookService.checkAccessToBook(BOOK_ID);

            verify(bookRepository).userIsAuthorOfBook(BOOK_ID, USER_ID);
            verifyNoMoreInteractions(bookRepository);
        }
    }


    @Test
    void createBook_shouldReturnBookReadDto_WhenBookIsCreatedSuccessfully() {
        BookSaveDto bookSaveDto = createBookSaveDto(List.of(USER_ID), SERIES_ID);
        Book book = createBook(PublicationStatus.COMPLETED);
        Book savedBook = createBook(PublicationStatus.COMPLETED);
        BookReadDto expectedBookReadDto = createBookReadDto(PublicationStatus.COMPLETED);

        User author = createUser(USER_ID);

        when(bookMapper.toEntity(bookSaveDto)).thenReturn(book);
        when(userService.findById(USER_ID)).thenReturn(author);
        when(bookRepository.save(book)).thenReturn(savedBook);
        when(bookMapper.toDto(savedBook)).thenReturn(expectedBookReadDto);

        BookReadDto result = bookService.createBook(bookSaveDto);

        assertEquals(expectedBookReadDto, result);
        verify(bookRepository).save(book);
        verify(bookMapper).toEntity(bookSaveDto);
        verify(bookMapper).toDto(savedBook);
        verify(userService).findById(USER_ID);
        verify(bookRepository).saveBookAuthorsInBatch(BOOK_ID, List.of(USER_ID));
        verify(bookRepository).saveGenresInBatch(BOOK_ID, bookSaveDto.genres());
        verify(bookRepository).saveTagsInBatch(BOOK_ID, bookSaveDto.tags());
        verify(eventPublisher).publishEvent(new BookCreatedEvent(savedBook, List.of(author)));
        verifyNoMoreInteractions(bookRepository, userService, eventPublisher);
    }

    @Test
    void createBook_shouldThrowResourceNotFoundException_WhenAuthorIdsNotFound() {
        BookSaveDto bookSaveDto = createBookSaveDto(List.of(USER_ID), SERIES_ID);

        when(userService.findById(USER_ID)).thenThrow(ResourceNotFoundException.class);

        assertThrows(ResourceNotFoundException.class, () -> bookService.createBook(bookSaveDto));
        verify(userService).findById(USER_ID);
        verifyNoInteractions(bookRepository, eventPublisher);
    }

    @Test
    void createBook_shouldHandleBookSeries_WhenSeriesIdIsPresent() {
        BookSaveDto bookSaveDto = createBookSaveDto(List.of(USER_ID), SERIES_ID);
        BookSeries bookSeries = createBookSeries(SERIES_ID);
        Book book = createBook(PublicationStatus.COMPLETED);
        User user = createUser(USER_ID);
        BookReadDto bookReadDto = createBookReadDto(PublicationStatus.IN_PROGRESS);

        when(bookMapper.toEntity(bookSaveDto)).thenReturn(book);
        when(userService.findById(USER_ID)).thenReturn(user);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(bookReadDto);
        when(bookSeriesService.findById(SERIES_ID)).thenReturn(bookSeries);

        bookService.createBook(bookSaveDto);

        assertEquals(bookSeries, book.getBookSeries());
        verify(bookSeriesService).findById(SERIES_ID);
        verify(bookRepository).save(book);
        verify(userService).findById(USER_ID);
        verify(bookMapper).toDto(book);
        verify(bookMapper).toEntity(bookSaveDto);
        verify(bookRepository).saveBookAuthorsInBatch(BOOK_ID, List.of(USER_ID));
        verify(bookRepository).saveGenresInBatch(BOOK_ID, new ArrayList<>(book.getGenres()));
        verify(bookRepository).saveTagsInBatch(BOOK_ID, new ArrayList<>(book.getTags()));
        verifyNoMoreInteractions(bookRepository);
    }


    @Test
    void publishBook_shouldPublishBook_whenBookHasPublishedChapters() {
        Book book = createBook(PublicationStatus.IS_DRAFT);
        BookReadDto bookReadDto = createBookReadDto(PublicationStatus.IN_PROGRESS);

        when(bookRepository.existsPublishedChapterById(BOOK_ID)).thenReturn(true);
        when(bookRepository.findBookByIdEager(BOOK_ID)).thenReturn(Optional.of(book));
        when(bookMapper.toDto(book)).thenReturn(bookReadDto);

        BookReadDto result = bookService.publishBook(BOOK_ID);

        assertEquals(result, bookReadDto);
        assertEquals(PublicationStatus.IN_PROGRESS, book.getStatus());
        verify(bookRepository).existsPublishedChapterById(BOOK_ID);
        verify(bookRepository).findBookByIdEager(BOOK_ID);
        verify(bookRepository).save(book);
        verify(bookStatsService).updatePublicationDate(BOOK_ID);
        verify(bookMapper).toDto(book);
        verifyNoMoreInteractions(bookRepository, bookMapper, bookStatsService);
    }

    @Test
    void publishBook_shouldThrowException_whenNoPublishedChaptersExist() {
        when(bookRepository.existsPublishedChapterById(BOOK_ID)).thenReturn(false);

        assertThatThrownBy(() -> bookService.publishBook(BOOK_ID))
                .isInstanceOf(BadRequestBaseException.class)
                .hasMessage("Book must contain at least one published chapter");

        verify(bookRepository).existsPublishedChapterById(BOOK_ID);
        verifyNoMoreInteractions(bookRepository, bookMapper, bookStatsService);
    }

    @Test
    void publishBook_shouldThrowException_whenBookIsNotFound() {
        when(bookRepository.existsPublishedChapterById(BOOK_ID)).thenReturn(true);
        when(bookRepository.findBookByIdEager(BOOK_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.publishBook(BOOK_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(BOOK_NOT_FOUND.formatted(BOOK_ID));

        verify(bookRepository).existsPublishedChapterById(BOOK_ID);
        verify(bookRepository).findBookByIdEager(BOOK_ID);
        verifyNoMoreInteractions(bookRepository, bookMapper, bookStatsService);
    }

    @Test
    void publishBook_shouldThrowException_whenBookIsAlreadyPublished() {
        Book book = createBook(PublicationStatus.IN_PROGRESS);

        when(bookRepository.existsPublishedChapterById(BOOK_ID)).thenReturn(true);
        when(bookRepository.findBookByIdEager(BOOK_ID)).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> bookService.publishBook(BOOK_ID))
                .isInstanceOf(BadRequestBaseException.class)
                .hasMessage("Book with id [%d] is already published".formatted(BOOK_ID));

        verify(bookRepository).existsPublishedChapterById(BOOK_ID);
        verify(bookRepository).findBookByIdEager(BOOK_ID);
        verifyNoMoreInteractions(bookRepository, bookMapper, bookStatsService);
    }

    @Test
    void updateBook_shouldUpdateBook_whenBookExists() {
        Book book = createBook(PublicationStatus.IS_DRAFT);
        Book updatedBook = createBook(PublicationStatus.IN_PROGRESS);
        BookSaveDto dto = createBookSaveDto(List.of(USER_ID), null);
        BookReadDto bookReadDto = createBookReadDto(PublicationStatus.IN_PROGRESS);

        when(bookRepository.findBookByIdEager(BOOK_ID)).thenReturn(Optional.of(book));
        when(bookMapper.updateEntity(dto, book)).thenReturn(updatedBook);
        when(bookRepository.save(updatedBook)).thenReturn(updatedBook);
        when(bookMapper.toDto(updatedBook)).thenReturn(bookReadDto);

        BookReadDto result = bookService.updateBook(BOOK_ID, dto);

        assertEquals(bookReadDto, result);
        verify(bookRepository).findBookByIdEager(BOOK_ID);
        verify(bookMapper).updateEntity(dto, book);
        verify(bookRepository).save(updatedBook);
        verify(bookMapper).toDto(updatedBook);
        verify(bookStatsService).updateBookStatistics(BOOK_ID);
        verifyNoInteractions(elasticBookService);
        verifyNoMoreInteractions(bookRepository, bookMapper, bookStatsService, elasticBookService);
    }

    @Test
    void updateBook_shouldThrowResourceNotFoundException_whenBookNotFound() {
        BookSaveDto dto = createBookSaveDto(List.of(USER_ID), null);
        when(bookRepository.findBookByIdEager(BOOK_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookService.updateBook(BOOK_ID, dto));

        verify(bookRepository).findBookByIdEager(BOOK_ID);
        verifyNoMoreInteractions(bookRepository, bookMapper, bookStatsService, elasticBookService);
    }

    @Test
    void deleteBook_shouldDeleteBook_whenBookExists() {
        Book book = createBook(PublicationStatus.IS_DRAFT);
        when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.of(book));

        bookService.deleteBook(BOOK_ID);

        verify(bookRepository).findById(BOOK_ID);
        verify(bookRepository).delete(book);
        verify(elasticBookService).deleteById(BOOK_ID);
        verifyNoMoreInteractions(bookRepository, elasticBookService);
    }

    @Test
    void deleteBook_shouldThrowResourceNotFoundException_whenBookNotFound() {
        when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookService.deleteBook(BOOK_ID));

        verify(bookRepository).findById(BOOK_ID);
        verifyNoMoreInteractions(bookRepository, elasticBookService);
    }


    private Book createBook(PublicationStatus status) {
        return Book.builder()
                .id(BOOK_ID)
                .title("book")
                .form(BookForm.NOVEL)
                .status(status)
                .price(BigDecimal.valueOf(99.99))
                .genres(Set.of(Genre.FANTASY))
                .annotation("annotation")
                .tags(Set.of("tag"))
                .authorNote("author's note")
                .coverUrl("http://localhost:8080/cover.jpg")
                .accessType(AccessType.FREE)
                .build();
    }

    private BookReadDto createBookReadDto(PublicationStatus status) {
        BookReadDto bookReadDto = new BookReadDto(
                BOOK_ID,
                "book",
                BookForm.NOVEL,
                AccessType.FREE,
                status,
                BigDecimal.valueOf(99.99),
                "annotation",
                "author's note",
                "http://localhost:8080/cover.jpg",
                null,
                null);
        bookReadDto.setGenres(Set.of(Genre.FANTASY));
        bookReadDto.setTags(Set.of("tag1", "tag2"));
        bookReadDto.setAuthors(Set.of(new AuthorWithAvatarDto(USER_ID, "Author", "http://localhost:8080/avatar.jpg")));
        return bookReadDto;
    }

    private BookSaveDto createBookSaveDto(List<Long> authorIds, Long seriesId) {
        return new BookSaveDto(
                "book",
                BookForm.NOVEL,
                "annotation",
                "author's note",
                seriesId,
                List.of(Genre.FANTASY),
                List.of("tag"),
                authorIds
        );
    }

    private User createUser(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setUsername("username");
        user.setAvatarUrl("avatarUrl");
        return user;
    }

    private BookSeries createBookSeries(Long seriesId) {
        BookSeries series = new BookSeries();
        series.setId(seriesId);
        return series;
    }
}
