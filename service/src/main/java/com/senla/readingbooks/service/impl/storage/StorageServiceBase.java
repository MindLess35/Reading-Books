package com.senla.readingbooks.service.impl.storage;

import com.senla.readingbooks.property.MinioProperty;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.UUID;

import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

@RequiredArgsConstructor
public abstract class StorageServiceBase {
    protected final MinioProperty minioProperty;
    protected final MinioClient minioClient;
    public static final Set<String> IMAGE = Set.of(IMAGE_JPEG_VALUE, IMAGE_PNG_VALUE);

    protected String getExtension(String contentType) {
        return contentType.substring(contentType.lastIndexOf('/')).replace('/', '.');
    }

    protected String extractObjectIdFromUrl(String objectUrl) {
        return objectUrl.substring(objectUrl.lastIndexOf('/') + 1);
    }

    protected String buildObjectUrl(String objectId, String bucketKey) {
        String propertyUrl = minioProperty.getUrl();
        String url = propertyUrl.equals("minio") ? "localhost" : propertyUrl;
        return url + "/" + minioProperty.getBuckets().get(bucketKey) + "/" + objectId;
    }

    protected String buildObjectPrefix(Long id) {
        return id + "_";
    }

    protected String getUuidAsString() {
        return UUID.randomUUID().toString();
    }

}
