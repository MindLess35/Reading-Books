package com.senla.readingbooks.enums.book;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PublicationStatus {
    COMPLETED("completed"),

    IN_PROGRESS("in_progress"),

    ABANDONED("abandoned"),

    IS_DRAFT("is_draft");

    private final String value;

    PublicationStatus(String value) {
        this.value = value;
    }


    @JsonCreator
    public static PublicationStatus fromValue(String value) {
        for (PublicationStatus status : PublicationStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown publication status: " + value);
    }
}

