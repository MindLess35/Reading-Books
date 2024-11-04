package com.senla.readingbooks.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum MediaEntityType {
    CHAPTER("chapter-media"),
    REVIEW("review-media"),
    COMMENT("comment-media");

    private final String bucketMediaKey;

    MediaEntityType(String bucketMediaKey) {
        this.bucketMediaKey = bucketMediaKey;
    }

    public String getBucketMediaKey() {
        return bucketMediaKey;
    }

    @JsonCreator
    public static MediaEntityType fromValue(String value) {
        for (MediaEntityType type : MediaEntityType.values()) {
            if (type.bucketMediaKey.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown bucket media key: " + value);
    }
}