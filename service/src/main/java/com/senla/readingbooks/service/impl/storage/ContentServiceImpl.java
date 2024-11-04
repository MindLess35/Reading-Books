package com.senla.readingbooks.service.impl.storage;

import com.senla.readingbooks.enums.ContentEntityType;
import com.senla.readingbooks.exception.StorageOperationException;
import com.senla.readingbooks.property.MinioProperty;
import com.senla.readingbooks.service.interfaces.storage.ContentService;
import com.senla.readingbooks.util.JsoupCleaner;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.springframework.http.MediaType.TEXT_HTML_VALUE;

@Service
public class ContentServiceImpl extends StorageServiceBase implements ContentService {
    public ContentServiceImpl(MinioProperty minioProperty, MinioClient minioClient) {
        super(minioProperty, minioClient);
    }

    @Override
    public String saveContentToStorage(String htmlContent, ContentEntityType contentEntityType, String contentUrl) {
        String cleanedContent = JsoupCleaner.cleanHtmlContent(htmlContent);
        String contentId = getUuidAsString() + getExtension(TEXT_HTML_VALUE);
        if (contentUrl != null)
            contentId = extractObjectIdFromUrl(contentUrl);

        try (InputStream inputStream = new ByteArrayInputStream(cleanedContent.getBytes(StandardCharsets.UTF_8))) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperty.getBuckets().get(contentEntityType.getBucketContentKey()))
                    .object(contentId)
                    .stream(inputStream, inputStream.available(), -1)
                    .contentType(TEXT_HTML_VALUE)
                    .build());
        } catch (Exception e) {
            throw new StorageOperationException("Save content failed, reason: " + e.getMessage());
        }
        return buildObjectUrl(contentId, contentEntityType.getBucketContentKey());
    }

    @Override
    public void deleteContentFromStorage(String contentUrl, ContentEntityType contentEntityType) {
        String contentId = extractObjectIdFromUrl(contentUrl);
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioProperty.getBuckets().get(contentEntityType.getBucketContentKey()))
                    .object(contentId)
                    .build());
        } catch (Exception e) {
            throw new StorageOperationException("Delete content failed, reason: " + e.getMessage());
        }
    }

}
