package com.senla.readingbooks.service.impl.storage;

import com.senla.readingbooks.dto.MediaUrlsDto;
import com.senla.readingbooks.enums.MediaEntityType;
import com.senla.readingbooks.enums.user.Role;
import com.senla.readingbooks.exception.StorageOperationException;
import com.senla.readingbooks.exception.base.BadRequestBaseException;
import com.senla.readingbooks.exception.base.ResourceNotFoundException;
import com.senla.readingbooks.property.MinioProperty;
import com.senla.readingbooks.repository.jpa.CommentRepository;
import com.senla.readingbooks.repository.jpa.book.BookReviewRepository;
import com.senla.readingbooks.repository.jpa.book.ChapterRepository;
import com.senla.readingbooks.service.interfaces.storage.MediaService;
import com.senla.readingbooks.util.AuthUtil;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.senla.readingbooks.property.CacheProperty.CACHE_SEPARATOR;
import static com.senla.readingbooks.service.impl.CommentServiceImpl.CACHE_COMMENT_EXISTS_ID;
import static com.senla.readingbooks.service.impl.book.BookReviewServiceImpl.CACHE_REVIEW_EXISTS_ID;
import static com.senla.readingbooks.service.impl.book.ChapterServiceImpl.CACHE_CHAPTER_EXISTS_ID;
import static org.springframework.http.MediaType.IMAGE_GIF_VALUE;

@Slf4j
@Service
public class MediaServiceImpl extends StorageServiceBase implements MediaService {
    private final CommentRepository commentRepository;
    private final ChapterRepository chapterRepository;
    private final BookReviewRepository bookReviewRepository;
    private final TransactionTemplate transactionTemplate;
    private final RedisTemplate<String, Boolean> redisTemplate;

    public MediaServiceImpl(MinioProperty minioProperty,
                            MinioClient minioClient,
                            CommentRepository commentRepository,
                            ChapterRepository chapterRepository,
                            BookReviewRepository bookReviewRepository,
                            TransactionTemplate transactionTemplate,
                            RedisTemplate<String, Boolean> redisTemplate) {
        super(minioProperty, minioClient);
        this.commentRepository = commentRepository;
        this.chapterRepository = chapterRepository;
        this.bookReviewRepository = bookReviewRepository;
        this.transactionTemplate = transactionTemplate;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public String uploadMedia(Long id, MultipartFile media, MediaEntityType entityType) {
        validateMedia(media, entityType);
        checkEntityExistence(id, entityType);

        String mediaIdWithPrefix = buildObjectPrefix(id) + getUuidAsString() + getExtension(media.getContentType());
        saveMediaToStorage(media, entityType, mediaIdWithPrefix);
        return buildObjectUrl(mediaIdWithPrefix, entityType.getBucketMediaKey());
    }

    @Override
    public void deleteMediaByUrls(Long id, MediaUrlsDto mediaUrlsDto, MediaEntityType entityType) {
        List<String> mediaUrls = mediaUrlsDto.mediaUrls();
        String prefix = buildObjectPrefix(id);
        List<String> validMediaIds = mediaUrls.stream()
                .map(this::extractObjectIdFromUrl)
                .filter(mediaId -> mediaId.startsWith(prefix))
                .toList();

        if (validMediaIds.size() != mediaUrls.size())
            throw new BadRequestBaseException("Url array contains invalid values");

        checkEntityExistence(id, entityType);
        List<DeleteObject> objectsToDelete = validMediaIds.stream()
                .map(DeleteObject::new)
                .toList();
        removeObjects(entityType.getBucketMediaKey(), objectsToDelete);
    }

    @Override
    public void deleteMediaByPrefix(Long id, MediaEntityType mediaEntityType) {
        String prefix = buildObjectPrefix(id);
        Iterable<Result<Item>> objectList = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(minioProperty.getBuckets().get(mediaEntityType.getBucketMediaKey()))
                .prefix(prefix)
                .build());
        if (!objectList.iterator().hasNext())
            return;

        List<DeleteObject> objectsToDelete = new ArrayList<>();
        try {
            for (Result<Item> result : objectList) {
                Item item = result.get();
                objectsToDelete.add(new DeleteObject(item.objectName()));
            }

            removeObjects(mediaEntityType.getBucketMediaKey(), objectsToDelete);
        } catch (Exception e) {
            throw new StorageOperationException("Delete media failed, reason: " + e.getMessage());
        }
    }

    private void removeObjects(String mediaBucketKey, List<DeleteObject> objectsToDelete) {
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(RemoveObjectsArgs.builder()
                .bucket(minioProperty.getBuckets().get(mediaBucketKey))
                .objects(objectsToDelete)
                .build());

        try {
            for (Result<DeleteError> errorResult : results) {
                DeleteError error = errorResult.get();
                log.error("Error in deleting object " + error.objectName() + "; Reason: " + error.message());
            }
        } catch (Exception e) {
            throw new StorageOperationException("Failed to delete media by URLs, reason: " + e.getMessage());
        }
    }

    private void checkEntityExistence(Long id, MediaEntityType entityType) {
        String cacheKey = switch (entityType) {
            case CHAPTER -> CACHE_CHAPTER_EXISTS_ID + CACHE_SEPARATOR + id;
            case REVIEW -> CACHE_REVIEW_EXISTS_ID + CACHE_SEPARATOR + id;
            case COMMENT -> CACHE_COMMENT_EXISTS_ID + CACHE_SEPARATOR + id;
        };
        Boolean isEntityExists = redisTemplate.opsForValue().get(cacheKey);

        if (isEntityExists == null) {
            transactionTemplate.setReadOnly(true);
            isEntityExists = switch (entityType) {
                case CHAPTER -> transactionTemplate.execute(status -> chapterRepository.existsById(id));
                case REVIEW -> transactionTemplate.execute(status -> bookReviewRepository.existsById(id));
                case COMMENT -> transactionTemplate.execute(status -> commentRepository.existsById(id));
            };

            if (isEntityExists != null) {
                redisTemplate.opsForValue().set(cacheKey, isEntityExists, 600L, TimeUnit.SECONDS);
            }
        }

        if (!Boolean.TRUE.equals(isEntityExists)) {
            throw new ResourceNotFoundException("%s with id [%d] not found".formatted(entityType.name().toLowerCase(), id));
        }
    }

    private void saveMediaToStorage(MultipartFile media, MediaEntityType entityType, String mediaIdWithPrefix) {
        try (InputStream inputStream = media.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperty.getBuckets().get(entityType.getBucketMediaKey()))
                    .object(mediaIdWithPrefix)
                    .stream(inputStream, media.getSize(), -1)
                    .contentType(media.getContentType())
                    .build());

        } catch (Exception e) {
            throw new StorageOperationException("Save media failed, reason: " + e.getMessage());
        }
    }

    private void validateMedia(MultipartFile media, MediaEntityType entityType) {
        String contentType = media.getContentType();
        if (media.isEmpty() || contentType == null) {
            throw new BadRequestBaseException("Media must be present and have a content type");
        }

        boolean isImage = IMAGE.contains(contentType);
        boolean isGif = Set.of("image/webp", IMAGE_GIF_VALUE).contains(contentType);
        boolean isVideo = Set.of("video/mp4", "video/webm").contains(contentType);
        boolean isImageOrGif = isImage || isGif;
        String entityTypeAsString = entityType.name().toLowerCase();
        switch (entityType) {
            case CHAPTER -> {
                if (!isImageOrGif) {
                    throw new BadRequestBaseException("Media in %s must have content with type image/jpeg, image/png, image/webp or image/gif".formatted(entityTypeAsString));
                }
            }
            case REVIEW, COMMENT -> {
                if (!isImageOrGif && !isVideo) {
                    throw new BadRequestBaseException("Media in %s must have content with type image/jpeg, image/png ,image/webp, image/gif, video/webm or video/mp4".formatted(entityTypeAsString));
                }
            }
        }
    }

    @Override
    public void checkAccessToEditMediaInEntity(Long entityId, MediaEntityType mediaEntityType) {
        Role role = AuthUtil.getAuthenticatedUserRole();
        if (role == Role.MODERATOR)
            return;
        Long authorizedUserId = AuthUtil.getAuthenticatedUserId();
        checkUserIsAuthorOfEntity(mediaEntityType, entityId, authorizedUserId);
    }

    private void checkUserIsAuthorOfEntity(MediaEntityType mediaEntityType, Long entityId, Long userId) {
        boolean isAuthor = switch (mediaEntityType) {
            case CHAPTER -> chapterRepository.userIsAuthorOfChapter(entityId, userId);
            case REVIEW -> bookReviewRepository.userIsAuthorOfReview(entityId, userId);
            case COMMENT -> commentRepository.userIsAuthorOfComment(entityId, userId);
        };

        if (!isAuthor) {
            throw new AccessDeniedException("Unauthorized access to media for a [%s] with id [%d] by a user with id [%d]"
                    .formatted(mediaEntityType.name().toLowerCase(), entityId, userId));
        }
    }

}
