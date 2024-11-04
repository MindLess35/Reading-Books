package com.senla.readingbooks.exception.response;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Getter
@SuperBuilder
@RequiredArgsConstructor
public abstract class BaseResponseErrorBody {

    @Builder.Default
    private final Instant timestamp = Instant.now();

    private final int status;

    private final String cause;

    private String exception;

    private String path;

}
