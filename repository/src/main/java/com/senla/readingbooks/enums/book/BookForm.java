package com.senla.readingbooks.enums.book;

import com.fasterxml.jackson.annotation.JsonCreator;


public enum BookForm {
    NOVEL("novel"),
    NOVELLA("novella"),
    STORY("story");

    private final String value;

    BookForm(String value) {
        this.value = value;
    }


    @JsonCreator
    public static BookForm fromValue(String value) {
        for (BookForm form : BookForm.values()) {
            if (form.value.equalsIgnoreCase(value)) {
                return form;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }
}
