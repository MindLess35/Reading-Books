package com.senla.readingbooks.service.impl.book;

import com.senla.readingbooks.dto.ChapterReadDto;
import com.senla.readingbooks.dto.ChapterSaveDto;
import com.senla.readingbooks.entity.book.Book;
import com.senla.readingbooks.entity.book.Chapter;
import com.senla.readingbooks.enums.ContentEntityType;
import com.senla.readingbooks.enums.MediaEntityType;
import com.senla.readingbooks.enums.user.Role;
import com.senla.readingbooks.exception.base.BadRequestBaseException;
import com.senla.readingbooks.exception.base.ResourceNotFoundException;
import com.senla.readingbooks.mapper.ChapterMapper;
import com.senla.readingbooks.repository.jpa.book.BookRepository;
import com.senla.readingbooks.repository.jpa.book.ChapterRepository;
import com.senla.readingbooks.service.interfaces.book.BookService;
import com.senla.readingbooks.service.interfaces.book.ChapterService;
import com.senla.readingbooks.service.interfaces.book.ChapterStatsService;
import com.senla.readingbooks.service.interfaces.storage.ContentService;
import com.senla.readingbooks.service.interfaces.storage.MediaService;
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

import java.time.Instant;

import static com.senla.readingbooks.property.CacheProperty.CACHE_SEPARATOR;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChapterServiceImpl implements ChapterService {
    private final ChapterRepository chapterRepository;
    private final BookRepository bookRepository;
    private final ChapterMapper chapterMapper;
    private final ChapterStatsService chapterStatsService;
    private final BookService bookService;
    private final ContentService contentService;
    private final MediaService mediaService;
    private final RedisTemplate<String, ChapterReadDto> redisTemplate;
    private static final String CHAPTER_NOT_FOUND = "Chapter with id [%d] not found";
    public static final String CACHE_CHAPTER_EXISTS_ID = "chapter::exists::id";
    private static final String CACHE_CHAPTER_DTO_ID = "chapter::dto::id";

    @Override
    public ChapterReadDto findChapterById(Long id) {
        String cacheKey = CACHE_CHAPTER_DTO_ID + CACHE_SEPARATOR + id;
        ChapterReadDto chapterReadDto = redisTemplate.opsForValue().get(cacheKey);
        if (chapterReadDto == null) {
            chapterReadDto = chapterRepository.findById(id)
                    .map(chapterMapper::toDto)
                    .orElseThrow(() -> new ResourceNotFoundException(CHAPTER_NOT_FOUND.formatted(id)));

            redisTemplate.opsForValue().set(cacheKey, chapterReadDto);
        }

        if (chapterReadDto.isDraft()) {
            if (AuthUtil.isUserAlreadyAuthenticated()) {
                checkAccessToChapter(id);
            } else {
                throw new AccessDeniedException("Chapter with id [%d] is a draft only its author can access".formatted(id));
            }
        }
        return chapterReadDto;
    }

    @Override
    @Cacheable(value = CACHE_CHAPTER_EXISTS_ID, key = "#id")
    public boolean existsById(Long id) {
        return chapterRepository.existsById(id);
    }

    @Override
    public Page<ChapterReadDto> findAllChaptersByBookId(Long bookId, Pageable pageable) {
        return chapterRepository.findAllByBookId(bookId, pageable)
                .map(chapterMapper::toDto);
    }

    @Override
    @Transactional
    @CachePut(value = CACHE_CHAPTER_DTO_ID, key = "#result.id")
    public ChapterReadDto createChapter(ChapterSaveDto dto) {
        Book book = bookService.findByIdLazy(dto.bookId());
        Chapter chapter = chapterMapper.toEntity(dto);
        String contentUrl = contentService.saveContentToStorage(dto.htmlContent(), ContentEntityType.CHAPTER, chapter.getContentUrl());

        chapter.setBook(book);
        chapter.setContentUrl(contentUrl);
        Chapter savedChapter = chapterRepository.save(chapter);
        chapterStatsService.createChapterStatistics(savedChapter);
        return chapterMapper.toDto(savedChapter);
    }

    @Override
    @Transactional
    @CachePut(value = CACHE_CHAPTER_DTO_ID, key = "#id")
    public ChapterReadDto publishChapter(Long id) {
        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CHAPTER_NOT_FOUND.formatted(id)));
        if (!chapter.getIsDraft()) {
            throw new BadRequestBaseException("Chapter with id [%d] is already published".formatted(id));
        }
        chapter.setIsDraft(false);
        chapter.setPublicationDate(Instant.now());
        return chapterMapper.toDto(chapterRepository.save(chapter));
    }

    @Override
    @Transactional
    @CachePut(value = CACHE_CHAPTER_DTO_ID, key = "#id")
    public ChapterReadDto updateChapter(Long id, ChapterSaveDto dto) {
        Chapter chapter = chapterRepository.findById(id)
                .map(ch -> chapterMapper.updateEntity(dto, ch))
                .orElseThrow(() -> new ResourceNotFoundException(CHAPTER_NOT_FOUND.formatted(id)));

        String htmlContent = dto.htmlContent();
        if (htmlContent != null) {
            contentService.saveContentToStorage(htmlContent, ContentEntityType.CHAPTER, chapter.getContentUrl());
        }
        chapterStatsService.updateChapterStatistics(id);
        return chapterMapper.toDto(chapterRepository.save(chapter));
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CACHE_CHAPTER_DTO_ID, key = "#id"),
            @CacheEvict(value = CACHE_CHAPTER_EXISTS_ID, key = "#id")})
    public void deleteChapter(Long id) {
        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CHAPTER_NOT_FOUND.formatted(id)));
        chapterRepository.delete(chapter);
        contentService.deleteContentFromStorage(chapter.getContentUrl(), ContentEntityType.CHAPTER);
        mediaService.deleteMediaByPrefix(id, MediaEntityType.CHAPTER);
    }

    @Override
    public void checkAccessToChapter(Long chapterId) {
        Role role = AuthUtil.getAuthenticatedUserRole();
        if (role == Role.MODERATOR)
            return;

        Long authorizedUserId = AuthUtil.getAuthenticatedUserId();
        if (!isUserIsAuthorOfChapter(chapterId, authorizedUserId))
            throw new AccessDeniedException("Unauthorized access to a chapter with id [%d] by a user with id [%d] and the [%s] role. Or the book is a draft. Only its author can access the draft."
                    .formatted(chapterId, authorizedUserId, role));
    }

    @Override
    public boolean isUserIsAuthorOfChapter(Long chapterId, Long userId) {
        return chapterRepository.userIsAuthorOfChapter(chapterId, userId);
    }

    @Override
    public void checkAccessToCreateChapter(Long bookId) {
        Role role = AuthUtil.getAuthenticatedUserRole();

        Long authorizedUserId = AuthUtil.getAuthenticatedUserId();
        if (!bookRepository.userIsAuthorOfBook(bookId, authorizedUserId))
            throw new AccessDeniedException("Unauthorized attempt to create a chapter for book with id [%d] by a user with id [%d] and the [%s] role"
                    .formatted(bookId, authorizedUserId, role));
    }
}