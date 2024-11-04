package com.senla.readingbooks.service.impl.storage;

import com.senla.readingbooks.enums.ImageEntityType;
import com.senla.readingbooks.enums.user.Role;
import com.senla.readingbooks.exception.StorageOperationException;
import com.senla.readingbooks.exception.base.BadRequestBaseException;
import com.senla.readingbooks.exception.base.ResourceNotFoundException;
import com.senla.readingbooks.projection.EntityIdWithImageUrlProjection;
import com.senla.readingbooks.property.MinioProperty;
import com.senla.readingbooks.repository.jpa.book.BookRepository;
import com.senla.readingbooks.repository.jpa.user.UserRepository;
import com.senla.readingbooks.service.interfaces.storage.ImageService;
import com.senla.readingbooks.util.AuthUtil;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.function.LongFunction;

@Service
public class ImageServiceImpl extends StorageServiceBase implements ImageService {
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final TransactionTemplate transactionTemplate;

    public ImageServiceImpl(MinioProperty minioProperty,
                            MinioClient minioClient,
                            UserRepository userRepository,
                            BookRepository bookRepository,
                            TransactionTemplate transactionTemplate) {
        super(minioProperty, minioClient);
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.transactionTemplate = transactionTemplate;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public String uploadImage(MultipartFile image, Long id, ImageEntityType imageEntityType) {
        validateImage(image);
        // return type is ignored, because url is not required. In this case method just check entity existence
        checkEntityExistenceAndGetUrlIfExists(id, imageEntityType, false);

        String imageId = getUuidAsString() + getExtension(image.getContentType());
        saveImageToStorage(image, imageId, imageEntityType);

        String imageUrl = buildObjectUrl(imageId, imageEntityType.getBucketKey());
        saveImageUrlToEntity(id, imageEntityType, imageUrl);
        return imageUrl;
    }

    @Override
    public String updateImage(MultipartFile image, Long id, ImageEntityType imageEntityType) {
        validateImage(image);
        String existingUrl = checkEntityExistenceAndGetUrlIfExists(id, imageEntityType, true);
        String imageKey = extractObjectIdFromUrl(existingUrl);
        saveImageToStorage(image, imageKey, imageEntityType);
        return existingUrl;
    }

    @Override
    public void deleteImage(Long id, ImageEntityType imageEntityType) {
        String existingUrl = checkEntityExistenceAndGetUrlIfExists(id, imageEntityType, true);
        deleteImageFromStorage(existingUrl, imageEntityType);
        deleteImageUrlFromEntity(id, imageEntityType);
    }

    private String checkEntityExistenceAndGetUrlIfExists(Long id, ImageEntityType imageEntityType, boolean isUrlMustExists) {
        return switch (imageEntityType) {
            case USER ->
                    checkAndGetImageUrl(id, imageEntityType, userRepository::findAvatarWithUserIdById, isUrlMustExists);
            case BOOK ->
                    checkAndGetImageUrl(id, imageEntityType, bookRepository::findCoverWithBookIdById, isUrlMustExists);
        };
    }

    private String checkAndGetImageUrl(Long id,
                                       ImageEntityType imageEntityType,
                                       LongFunction<Optional<EntityIdWithImageUrlProjection>> findImageFunction,
                                       boolean isUrlMustExists) {
        transactionTemplate.setReadOnly(true);
        String entity = imageEntityType.name().toLowerCase();
        EntityIdWithImageUrlProjection projection = Objects.requireNonNull(transactionTemplate.execute(status
                        -> findImageFunction.apply(id)), "Execute readOnly query in transaction for checkAndGetImageUrl method in ImageServiceImpl class failed")
                .orElseThrow(() -> new ResourceNotFoundException("%s with id [%d] not found".formatted(entity, id)));

        String imageUrl = projection.getImageUrl();
        if (isUrlMustExists && imageUrl == null) {
            throw new BadRequestBaseException("%s with id [%d] has no image".formatted(entity, id));
        }

        if (!isUrlMustExists && imageUrl != null) {
            throw new BadRequestBaseException("%s with id [%d] already have image".formatted(entity, id));
        }
        return imageUrl;
    }

    @SuppressWarnings("java:S1301")
    private void saveImageUrlToEntity(Long id, ImageEntityType imageEntityType, String imageUrl) {
        transactionTemplate.setReadOnly(false);
        switch (imageEntityType) {
            case BOOK ->
                    transactionTemplate.executeWithoutResult(status -> bookRepository.updateCoverById(imageUrl, id));
            case USER ->
                    transactionTemplate.executeWithoutResult(status -> userRepository.updateAvatarById(imageUrl, id));
        }
    }

    @SuppressWarnings("java:S1301")
    private void deleteImageUrlFromEntity(Long id, ImageEntityType imageEntityType) {
        transactionTemplate.setReadOnly(false);
        switch (imageEntityType) {
            case BOOK -> transactionTemplate.executeWithoutResult(status -> bookRepository.updateCoverToNullById(id));
            case USER -> transactionTemplate.executeWithoutResult(status -> userRepository.updateAvatarToNullById(id));
        }
    }

    private void saveImageToStorage(MultipartFile image, String imageId, ImageEntityType imageEntityType) {
        try (InputStream inputStream = image.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperty.getBuckets().get(imageEntityType.getBucketKey()))
                    .object(imageId)
                    .stream(inputStream, image.getSize(), -1)
                    .contentType(image.getContentType())
                    .build());
        } catch (Exception e) {
            throw new StorageOperationException("Save image failed, reason: " + e.getMessage());
        }
    }

    private void deleteImageFromStorage(String imageUrl, ImageEntityType imageEntityType) {
        String objectKey = extractObjectIdFromUrl(imageUrl);
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioProperty.getBuckets().get(imageEntityType.getBucketKey()))
                    .object(objectKey)
                    .build());
        } catch (Exception e) {
            throw new StorageOperationException("Delete image failed, reason: " + e.getMessage());
        }
    }

    private void validateImage(MultipartFile image) {
        String contentType = image.getContentType();
        if (image.isEmpty() || contentType == null) {
            throw new BadRequestBaseException("Image must be present and have content type");
        }

        boolean isImage = IMAGE.contains(contentType);
        if (!isImage) {
            throw new BadRequestBaseException("Image must be of type image/jpeg or image/png");
        }
    }

    @Override
    public void checkAccessToEditImageInEntity(Long id, ImageEntityType imageEntityType) {
        Role role = AuthUtil.getAuthenticatedUserRole();
        if (role == Role.MODERATOR)
            return;
        Long authorizedUserId = AuthUtil.getAuthenticatedUserId();
        checkUserCanEditEntityImage(imageEntityType, id, authorizedUserId);
    }

    private void checkUserCanEditEntityImage(ImageEntityType imageEntityType, Long entityId, Long userId) {
        boolean isAuthor = switch (imageEntityType) {
            case USER -> Objects.equals(entityId, userId);
            case BOOK -> bookRepository.userIsAuthorOfBook(entityId, userId);
        };

        if (!isAuthor) {
            throw new AccessDeniedException("Unauthorized access to image for a [%s] with id [%d] by a user with id [%d]"
                    .formatted(imageEntityType.name().toLowerCase(), entityId, userId));
        }
    }
}
