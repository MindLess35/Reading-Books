package com.senla.readingbooks.service.impl.book;

import com.senla.readingbooks.dto.BookReviewReadDto;
import com.senla.readingbooks.dto.BookReviewSaveDto;
import com.senla.readingbooks.entity.book.Book;
import com.senla.readingbooks.entity.book.BookReview;
import com.senla.readingbooks.entity.user.User;
import com.senla.readingbooks.enums.ContentEntityType;
import com.senla.readingbooks.enums.MediaEntityType;
import com.senla.readingbooks.enums.user.Role;
import com.senla.readingbooks.exception.base.BadRequestBaseException;
import com.senla.readingbooks.exception.base.ResourceNotFoundException;
import com.senla.readingbooks.mapper.BookReviewMapper;
import com.senla.readingbooks.repository.jpa.book.BookReviewRepository;
import com.senla.readingbooks.service.interfaces.book.BookReviewService;
import com.senla.readingbooks.service.interfaces.book.BookService;
import com.senla.readingbooks.service.interfaces.storage.ContentService;
import com.senla.readingbooks.service.interfaces.storage.MediaService;
import com.senla.readingbooks.service.interfaces.user.UserService;
import com.senla.readingbooks.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookReviewServiceImpl implements BookReviewService {
    private final BookReviewRepository bookReviewRepository;
    private final UserService userService;
    private final BookService bookService;
    private final ContentService contentService;
    private final BookReviewMapper bookReviewMapper;
    private final MediaService mediaService;
    private static final String REVIEW_NOT_FOUND = "Review with id [%d] not found";
    private static final String CACHE_REVIEW_DTO_ID = "review::dto::id";
    public static final String CACHE_REVIEW_EXISTS_ID = "review::exists::id";

    @Override
    @Cacheable(value = CACHE_REVIEW_DTO_ID, key = "#id")
    public BookReviewReadDto findReviewById(Long id) {
        return bookReviewRepository.findById(id)
                .map(bookReviewMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException(REVIEW_NOT_FOUND.formatted(id)));
    }

    @Override
    @Transactional
    @CachePut(value = CACHE_REVIEW_DTO_ID, key = "#result.id")
    public BookReviewReadDto createReview(BookReviewSaveDto dto) {
        Long userId = dto.userId();
        User user = userService.findById(userId);
        Long bookId = dto.bookId();
        Book book = bookService.findByIdLazy(bookId);
        bookReviewRepository.findByUserIdAndBookId(userId, bookId)
                .ifPresent(bookReview -> {
                    throw new BadRequestBaseException("User with id [%d] has already written a review of a book with id [%d]".formatted(userId, bookId));
                });

        BookReview bookReview = bookReviewMapper.toEntity(dto);
        String contentUrl = contentService.saveContentToStorage(dto.htmlContent(), ContentEntityType.REVIEW, bookReview.getContentUrl());
        bookReview.setBook(book);
        bookReview.setUser(user);
        bookReview.setContentUrl(contentUrl);
        BookReview savedReview = bookReviewRepository.save(bookReview);
        return bookReviewMapper.toDto(savedReview);
    }

    @Override
    @Transactional
    @CachePut(value = CACHE_REVIEW_DTO_ID, key = "#id")
    public BookReviewReadDto updateReview(Long id, BookReviewSaveDto dto) {
        BookReview bookReview = bookReviewRepository.findById(id)
                .map(review -> bookReviewMapper.updateEntity(dto, review))
                .orElseThrow(() -> new ResourceNotFoundException(REVIEW_NOT_FOUND.formatted(id)));

        String htmlContent = dto.htmlContent();
        if (htmlContent != null)
            contentService.saveContentToStorage(htmlContent, ContentEntityType.REVIEW, bookReview.getContentUrl());

        BookReview savedReview = bookReviewRepository.saveAndFlush(bookReview);
        return bookReviewMapper.toDto(savedReview);
    }

    @Override
    @Cacheable(value = CACHE_REVIEW_EXISTS_ID, key = "#id")
    public boolean existsById(Long id) {
        return bookReviewRepository.existsById(id);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CACHE_REVIEW_DTO_ID, key = "#id"),
            @CacheEvict(value = CACHE_REVIEW_EXISTS_ID, key = "#id")})
    public void deleteReview(Long id) {
        BookReview review = bookReviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(REVIEW_NOT_FOUND.formatted(id)));
        bookReviewRepository.delete(review);
        contentService.deleteContentFromStorage(review.getContentUrl(), ContentEntityType.REVIEW);
        mediaService.deleteMediaByPrefix(id, MediaEntityType.REVIEW);
    }

    @Override
    public void checkAccessToEditReview(Long reviewId) {
        Role role = AuthUtil.getAuthenticatedUserRole();
        if (role == Role.MODERATOR)
            return;

        Long authorizedUserId = AuthUtil.getAuthenticatedUserId();
        if (!isUserIsAuthorOfReview(reviewId, authorizedUserId))
            throw new AccessDeniedException("Unauthorized access to a book review with id [%d] by a user with id [%d] and the [%s] role"
                    .formatted(reviewId, authorizedUserId, role));
    }

    @Override
    public boolean isUserIsAuthorOfReview(Long reviewId, Long userId) {
        return bookReviewRepository.userIsAuthorOfReview(reviewId, userId);
    }
}
