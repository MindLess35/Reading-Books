package com.senla.readingbooks.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ContentEntityType {
    CHAPTER("chapter-content"),
    REVIEW("review-content"),
    COMMENT("comment-content");

    private final String bucketContentKey;

    ContentEntityType(String bucketContentKey) {
        this.bucketContentKey = bucketContentKey;
    }

    public String getBucketContentKey() {
        return bucketContentKey;
    }

    @JsonCreator
    public static ContentEntityType fromValue(String value) {
        for (ContentEntityType type : ContentEntityType.values()) {
            if (type.bucketContentKey.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown bucket content key: " + value);
    }
}