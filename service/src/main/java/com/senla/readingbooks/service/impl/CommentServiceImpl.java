package com.senla.readingbooks.service.impl;

import com.senla.readingbooks.dto.HtmlContentDto;
import com.senla.readingbooks.dto.comment.CommentReadDto;
import com.senla.readingbooks.dto.comment.CommentSaveDto;
import com.senla.readingbooks.dto.comment.EntityIdAndTypeDto;
import com.senla.readingbooks.dto.comment.ReplyCommentWithUserDto;
import com.senla.readingbooks.dto.comment.RootCommentWithReplyAndUserDto;
import com.senla.readingbooks.entity.Comment;
import com.senla.readingbooks.entity.user.User;
import com.senla.readingbooks.enums.ContentEntityType;
import com.senla.readingbooks.enums.EntityType;
import com.senla.readingbooks.enums.MediaEntityType;
import com.senla.readingbooks.enums.user.Role;
import com.senla.readingbooks.exception.base.BadRequestBaseException;
import com.senla.readingbooks.exception.base.ResourceNotFoundException;
import com.senla.readingbooks.mapper.CommentMapper;
import com.senla.readingbooks.projection.CommentReplyCountProjection;
import com.senla.readingbooks.projection.CommentWithFirstReplyProjection;
import com.senla.readingbooks.repository.jpa.CommentRepository;
import com.senla.readingbooks.service.interfaces.CommentService;
import com.senla.readingbooks.service.interfaces.book.BookCollectionService;
import com.senla.readingbooks.service.interfaces.book.BookReviewService;
import com.senla.readingbooks.service.interfaces.book.BookSeriesService;
import com.senla.readingbooks.service.interfaces.book.BookService;
import com.senla.readingbooks.service.interfaces.book.ChapterService;
import com.senla.readingbooks.service.interfaces.storage.ContentService;
import com.senla.readingbooks.service.interfaces.storage.MediaService;
import com.senla.readingbooks.service.interfaces.user.UserService;
import com.senla.readingbooks.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final CommentMapper commentMapper;
    private final BookService bookService;
    private final BookCollectionService bookCollectionService;
    private final BookSeriesService bookSeriesService;
    private final ContentService contentService;
    private final BookReviewService bookReviewService;
    private final ChapterService chapterService;
    private final MediaService mediaService;
    private static final String COMMENT_NOT_FOUND = "Comment with id [%d] not found";
    private static final String CACHE_COMMENT_DTO_ID = "comment::dto::id";
    public static final String CACHE_COMMENT_EXISTS_ID = "review::exists::id";

    @Override
    @Cacheable(value = CACHE_COMMENT_DTO_ID, key = "#id")
    public CommentReadDto findCommentById(Long id) {
        return commentRepository.findById(id)
                .map(commentMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException(COMMENT_NOT_FOUND.formatted(id)));
    }

    @Override
    public Page<RootCommentWithReplyAndUserDto> findRootCommentsWithFirstReply(Pageable pageable, EntityIdAndTypeDto dto) {
        Long entityId = dto.entityId();
        EntityType entityType = dto.entityType();
        checkEntityExistence(entityType, entityId);

        int countRootComments = commentRepository.countRootComments(entityId, entityType);
        if (countRootComments == 0) {
            return Page.empty(pageable);
        }

        String entityTypeAsString = entityType.name();
        boolean isFirstPage = pageable.getPageNumber() == 0;
        List<CommentWithFirstReplyProjection> rootCommentsWithFirstReply;
        if (isFirstPage) {
            rootCommentsWithFirstReply = commentRepository.findRootCommentsWithFirstReply(entityId, entityTypeAsString, pageable);
        } else {
            rootCommentsWithFirstReply = commentRepository.findRootCommentsWithFirstReplyExcludingPinned(entityId, entityTypeAsString, pageable);
        }

        List<CommentReadDto> rootComments = commentMapper.toListDto(rootCommentsWithFirstReply);
        List<CommentReadDto> replyComments = commentMapper.toListReplyDto(rootCommentsWithFirstReply);

        boolean isPinnedPresentInSelection = rootComments.stream()
                .anyMatch(CommentReadDto::isPinned);

        if (isFirstPage && !isPinnedPresentInSelection)
            findPinnedComment(entityId, entityTypeAsString, rootComments, replyComments);

        List<Long> rootCommentIds = rootComments.stream()
                .map(CommentReadDto::id)
                .toList();

        List<Long> replyCommentIds = replyComments.stream()
                .map(CommentReadDto::id)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());


        replyCommentIds.addAll(rootCommentIds);
        List<User> users = commentRepository.findUsersByCommentIds(replyCommentIds);

        Map<Long, Long> repliesCountMap = commentRepository.countRepliesForRootComments(rootCommentIds).stream()
                .collect(Collectors.toMap(CommentReplyCountProjection::getRootCommentId,
                        CommentReplyCountProjection::getReplyCount));

        List<RootCommentWithReplyAndUserDto> result = new ArrayList<>();
        fillResult(rootComments, users, replyComments, repliesCountMap, result);

        return new PageImpl<>(result, pageable, countRootComments);
    }

    private void fillResult(List<CommentReadDto> rootComments,
                            List<User> users,
                            List<CommentReadDto> replyComments,
                            Map<Long, Long> repliesCountMap,
                            List<RootCommentWithReplyAndUserDto> result) {
        Map<Long, User> usersMap = users.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        for (CommentReadDto rootComment : rootComments) {
            User rootUser = usersMap.get(rootComment.userId());

            CommentReadDto firstReply = replyComments.stream()
                    .filter(reply -> reply.parentId() != null && reply.parentId().equals(rootComment.id()))
                    .findFirst()
                    .orElse(null);

            ReplyCommentWithUserDto firstReplyDto = null;
            if (firstReply != null) {
                User replyUser = usersMap.get(firstReply.userId());
                firstReplyDto = commentMapper.toReplyWithUserDto(firstReply, replyUser);
            }

            Long otherRepliesCount = repliesCountMap.get(rootComment.id());
            if (firstReplyDto != null) {
                otherRepliesCount--;
            }

            if (rootComment.isPinned()) {
                result.add(0, commentMapper.toCommentWithUserDto(rootComment, rootUser, firstReplyDto, otherRepliesCount));
            } else {
                result.add(commentMapper.toCommentWithUserDto(rootComment, rootUser, firstReplyDto, otherRepliesCount));
            }
        }
    }

    private void findPinnedComment(Long entityId,
                                   String entityTypeAsString,
                                   List<CommentReadDto> rootComments,
                                   List<CommentReadDto> replyComments) {

        CommentWithFirstReplyProjection pinnedCommentWithFirstReply = commentRepository
                .findPinnedCommentWithFirstReply(entityId, entityTypeAsString);

        if (pinnedCommentWithFirstReply != null) {
            CommentReadDto rootPinnedComment = commentMapper.mapRootComment(pinnedCommentWithFirstReply);
            CommentReadDto replyPinnedComment = commentMapper.mapReplyComment(pinnedCommentWithFirstReply);

            rootComments.remove(rootComments.size() - 1);
            rootComments.add(rootPinnedComment);

            replyComments.remove(replyComments.size() - 1);
            replyComments.add(replyPinnedComment);
        }

    }

    @Override
    @Transactional
    @CachePut(value = CACHE_COMMENT_DTO_ID, key = "#result.id")
    public CommentReadDto createComment(CommentSaveDto dto) {
        User user = userService.findById(dto.userId());
        Long entityId = dto.entityId();
        EntityType entityType = dto.entityType();
        checkEntityExistence(entityType, entityId);

        Long parentId = dto.parentId();
        Comment parentComment = Optional.ofNullable(parentId)
                .map(id -> commentRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(COMMENT_NOT_FOUND.formatted(parentId))))
                .orElse(null);

        if (parentComment != null && (!Objects.equals(parentComment.getEntityId(), entityId) || parentComment.getEntityType() != entityType))
            throw new BadRequestBaseException("Attempt to write a reply to a comment of the wrong entity");

        Comment comment = commentMapper.toEntity(dto);
        String contentUrl = contentService.saveContentToStorage(dto.htmlContent(), ContentEntityType.COMMENT, comment.getContentUrl());

        comment.setUser(user);
        comment.setContentUrl(contentUrl);
        comment.setParent(parentComment);
        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toDto(savedComment);
    }

    private void checkEntityExistence(EntityType entityType, Long entityId) {
        boolean isExists = switch (entityType) {
            case BOOK -> bookService.existsById(entityId);
            case CHAPTER -> chapterService.existsById(entityId);
            case BOOK_REVIEW -> bookReviewService.existsById(entityId);
            case BOOK_COLLECTION -> bookCollectionService.existsById(entityId);
            case BOOK_SERIES -> bookSeriesService.existsById(entityId);
        };

        if (!isExists) {
            throw new ResourceNotFoundException("Entity " + entityType.name().toLowerCase() + " with id " + entityId + " not found");
        }
    }

    @Override
    @Transactional
    @CachePut(value = CACHE_COMMENT_DTO_ID, key = "#result.id")
    public CommentReadDto updateComment(Long id, HtmlContentDto htmlContent) {
        CommentReadDto commentReadDto = commentRepository.findById(id)
                .map(c -> {
                    c.setUpdatedAt(Instant.now());
                    return c;
                })
                .map(commentRepository::save)
                .map(commentMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException(COMMENT_NOT_FOUND.formatted(id)));

        contentService.saveContentToStorage(htmlContent.htmlContent(), ContentEntityType.COMMENT, commentReadDto.contentUrl());
        return commentReadDto;
    }

    @Override
    @Cacheable(value = CACHE_COMMENT_EXISTS_ID, key = "#id")
    public boolean existsById(Long id) {
        return commentRepository.existsById(id);
    }

    @Override
    @Transactional
    @CacheEvict(value = CACHE_COMMENT_DTO_ID, key = "#id")
    public void deleteComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(COMMENT_NOT_FOUND.formatted(id)));
        commentRepository.delete(comment);
        contentService.deleteContentFromStorage(comment.getContentUrl(), ContentEntityType.COMMENT);
        mediaService.deleteMediaByPrefix(id, MediaEntityType.COMMENT);
    }

    @Override
    @Transactional
    @CachePut(value = CACHE_COMMENT_DTO_ID, key = "#id")
    public CommentReadDto pinComment(Long id, EntityIdAndTypeDto dto) {
        Long entityId = dto.entityId();
        EntityType entityType = dto.entityType();
        checkEntityExistence(entityType, entityId);
        Comment commentToPin = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(COMMENT_NOT_FOUND.formatted(id)));

        if (commentToPin.getParent() != null)
            throw new BadRequestBaseException(
                    "Comment with id [%d] is not a root comment. It is possible to pin only the root comments".formatted(id));

        commentRepository.findPinnedComment(entityId, entityType)
                .ifPresent(pinnedComment -> {
                    if (Objects.equals(pinnedComment.getId(), id))
                        throw new BadRequestBaseException("Comment with id [%d] already pinned".formatted(id));
                    pinnedComment.setIsPinned(false);
                    commentRepository.save(pinnedComment);
                });

        commentToPin.setIsPinned(true);
        Comment savedComment = commentRepository.save(commentToPin);
        return commentMapper.toDto(savedComment);
    }

    @Override
    @Transactional
    @CachePut(value = CACHE_COMMENT_DTO_ID, key = "#id")
    public CommentReadDto unpinComment(Long id, EntityIdAndTypeDto dto) {
        Long entityId = dto.entityId();
        EntityType entityType = dto.entityType();
        checkEntityExistence(entityType, entityId);
        Comment commentToUnpin = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(COMMENT_NOT_FOUND.formatted(id)));

        if (!commentToUnpin.getIsPinned()) {
            throw new BadRequestBaseException("Comment with id [%d] is not pinned".formatted(id));
        }

        commentToUnpin.setIsPinned(false);
        Comment savedComment = commentRepository.save(commentToUnpin);
        return commentMapper.toDto(savedComment);
    }

    @Override
    public void checkAccessToEditComment(Long id) {
        Role role = AuthUtil.getAuthenticatedUserRole();
        if (role == Role.MODERATOR)
            return;

        Long authorizedUserId = AuthUtil.getAuthenticatedUserId();
        if (!commentRepository.userIsAuthorOfComment(id, authorizedUserId))
            throw new AccessDeniedException("Unauthorized access to a comment with id [%d] by a user with id [%d] and the [%s] role"
                    .formatted(id, authorizedUserId, role));
    }

    @Override
    public void checkAccessToPinUnpinComment(EntityIdAndTypeDto dto) {
        Long authorizedUserId = AuthUtil.getAuthenticatedUserId();
        checkUserIsAuthorOfEntity(dto.entityType(), dto.entityId(), authorizedUserId);
    }

    private void checkUserIsAuthorOfEntity(EntityType entityType, Long entityId, Long userId) {
        boolean isAuthor = switch (entityType) {
            case BOOK -> bookService.isUserIsAuthorOfBook(entityId, userId);
            case CHAPTER -> chapterService.isUserIsAuthorOfChapter(entityId, userId);
            case BOOK_REVIEW -> bookReviewService.isUserIsAuthorOfReview(entityId, userId);
            case BOOK_COLLECTION -> bookCollectionService.isUserIsAuthorOfCollection(entityId, userId);
            case BOOK_SERIES -> bookSeriesService.isUserIsAuthorOfSeries(entityId, userId);
        };

        if (!isAuthor) {
            String entityTypeAsString = entityType.name().toLowerCase();
            throw new AccessDeniedException("Unauthorized attempt to pin or unpin comment to a %s with id [%d] by a user with id [%d]. Only author of the %s may pin/unpin comment."
                    .formatted(entityTypeAsString, entityId, userId, entityTypeAsString));
        }
    }
}

