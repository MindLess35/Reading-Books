package com.senla.readingbooks.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ImageEntityType {
    BOOK("book-cover"),
    USER("user-avatar");

    private final String bucketKey;

    ImageEntityType(String bucketKey) {
        this.bucketKey = bucketKey;
    }

    public String getBucketKey() {
        return bucketKey;
    }

    @JsonCreator
    public static ImageEntityType fromValue(String value) {
        for (ImageEntityType type : ImageEntityType.values()) {
            if (type.bucketKey.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown bucket key: " + value);
    }
}