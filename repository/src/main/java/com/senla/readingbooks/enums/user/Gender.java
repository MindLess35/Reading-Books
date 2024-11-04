package com.senla.readingbooks.enums.user;

import com.fasterxml.jackson.annotation.JsonCreator;


public enum Gender {
    F("f"),
    M("m");

    private final String value;

    Gender(String value) {
        this.value = value;
    }


    @JsonCreator
    public static Gender fromValue(String value) {
        for (Gender gender : Gender.values()) {
            if (gender.value.equalsIgnoreCase(value)) {
                return gender;
            }
        }
        throw new IllegalArgumentException("Unknown gender: " + value);
    }
}

