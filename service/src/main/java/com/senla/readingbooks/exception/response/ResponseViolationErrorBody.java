package com.senla.readingbooks.exception.response;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Setter
@Getter
@SuperBuilder
public class ResponseViolationErrorBody extends BaseResponseErrorBody {

    private Map<String, String> fieldErrors;

}
