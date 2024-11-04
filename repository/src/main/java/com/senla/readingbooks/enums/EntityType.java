package com.senla.readingbooks.enums;

import com.fasterxml.jackson.annotation.JsonCreator;


public enum EntityType {
    BOOK("book"),
    CHAPTER("chapter"),
    BOOK_REVIEW("book_review"),

    BOOK_COLLECTION("book_collection"),
    BOOK_SERIES("book_series");

    private final String value;

    EntityType(String value) {
        this.value = value;
    }


    @JsonCreator
    public static EntityType fromValue(String value) {
        for (EntityType type : EntityType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown entity type: " + value);
    }
}

