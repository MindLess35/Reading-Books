package com.senla.readingbooks.enums.user;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Role {
    READER("reader"),
    AUTHOR("author"),
    MODERATOR("moderator");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    @JsonCreator
    public static Role fromValue(String value) {
        for (Role role : Role.values()) {
            if (role.value.equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + value);
    }
}

