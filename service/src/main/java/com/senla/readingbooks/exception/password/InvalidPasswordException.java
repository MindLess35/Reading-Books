package com.senla.readingbooks.exception.password;

import lombok.Getter;

@Getter
public class InvalidPasswordException extends RuntimeException {
    private final String invalidFieldName;

    public InvalidPasswordException(String invalidFieldName, String message) {
        super(message);
        this.invalidFieldName = invalidFieldName;
    }
}
