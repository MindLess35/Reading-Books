package com.senla.readingbooks.enums.book;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AccessType {
    FREE("free"),
    PAID("paid");

    private final String value;


    @JsonCreator
    public static AccessType fromValue(String value) {
        for (AccessType accessType : AccessType.values()) {
            if (accessType.value.equalsIgnoreCase(value)) {
                return accessType;
            }
        }
        throw new IllegalArgumentException("Unknown access type: " + value);
    }
}

