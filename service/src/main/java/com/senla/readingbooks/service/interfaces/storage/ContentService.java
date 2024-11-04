package com.senla.readingbooks.service.interfaces.storage;

import com.senla.readingbooks.enums.ContentEntityType;

public interface ContentService {
    String saveContentToStorage(String htmlContent, ContentEntityType contentEntityType, String contentUrl);

    void deleteContentFromStorage(String contentUrl, ContentEntityType contentEntityType);
}
