package com.senla.readingbooks.service.interfaces.storage;

import com.senla.readingbooks.dto.MediaUrlsDto;
import com.senla.readingbooks.enums.MediaEntityType;
import org.springframework.web.multipart.MultipartFile;

public interface MediaService {
    String uploadMedia(Long id, MultipartFile media, MediaEntityType entityType);

    void deleteMediaByUrls(Long id, MediaUrlsDto mediaUrlsDto, MediaEntityType entityType);

    void deleteMediaByPrefix(Long id, MediaEntityType mediaEntityType);

    void checkAccessToEditMediaInEntity(Long entityId, MediaEntityType mediaEntityType);
}
