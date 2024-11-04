package com.senla.readingbooks.service.interfaces.storage;

import com.senla.readingbooks.enums.ImageEntityType;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    String uploadImage(MultipartFile file, Long id, ImageEntityType imageEntityType);

    String updateImage(MultipartFile file, Long id, ImageEntityType imageEntityType);

    void deleteImage(Long id, ImageEntityType entityType);

    void checkAccessToEditImageInEntity(Long id, ImageEntityType imageEntityType);
}
