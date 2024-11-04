package com.senla.readingbooks.enums.book;

import com.fasterxml.jackson.annotation.JsonCreator;


public enum LibrarySection {
    READING_NOW("reading_now"),
    WILL_READ("will_read"),
    ALREADY_READ("already_read"),
    NOT_INTERESTED("not_interested"),
    ABANDONED("abandoned");

    private final String value;

    LibrarySection(String value) {
        this.value = value;
    }


    @JsonCreator
    public static LibrarySection fromValue(String value) {
        for (LibrarySection section : LibrarySection.values()) {
            if (section.value.equalsIgnoreCase(value)) {
                return section;
            }
        }
        throw new IllegalArgumentException("Unknown library section: " + value);
    }
}

