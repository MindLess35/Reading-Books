package com.senla.readingbooks.exception.response;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class ResponseErrorBody extends BaseResponseErrorBody {

    private final String message;

}
