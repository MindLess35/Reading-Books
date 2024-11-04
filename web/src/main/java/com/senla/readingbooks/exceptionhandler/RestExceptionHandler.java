package com.senla.readingbooks.exceptionhandler;

import com.senla.readingbooks.exception.NoSuchOrderByFieldException;
import com.senla.readingbooks.exception.StorageOperationException;
import com.senla.readingbooks.exception.base.BadRequestBaseException;
import com.senla.readingbooks.exception.base.ResourceNotFoundException;
import com.senla.readingbooks.exception.password.InvalidPasswordException;
import com.senla.readingbooks.exception.response.ResponseErrorBody;
import com.senla.readingbooks.exception.response.ResponseViolationErrorBody;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.PAYLOAD_TOO_LARGE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Slf4j
@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler({Exception.class,
            StorageOperationException.class})
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public ResponseErrorBody handleUnexpectedException(Exception e, HttpServletRequest request) {
        log.error(e.getMessage(), e);
        return buildErrorBody(INTERNAL_SERVER_ERROR, e, request);
    }

    @ExceptionHandler
    @ResponseStatus(PAYLOAD_TOO_LARGE)
    public ResponseErrorBody handleMaxUploadSizeException(MaxUploadSizeExceededException e,
                                                          HttpServletRequest request) {
        return buildErrorBody(PAYLOAD_TOO_LARGE, e, request);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            BadRequestBaseException.class,
            HttpMediaTypeNotSupportedException.class,
            TypeMismatchException.class,
            NoSuchOrderByFieldException.class,
            PropertyReferenceException.class,
            InvalidDataAccessApiUsageException.class,
            MissingRequestValueException.class,
            HttpMediaTypeNotAcceptableException.class,
            NoResourceFoundException.class,
            HandlerMethodValidationException.class,})
    @ResponseStatus(BAD_REQUEST)
    public ResponseErrorBody handleBadRequestException(Exception e, HttpServletRequest request) {
        return buildErrorBody(BAD_REQUEST, e, request);
    }

    @ExceptionHandler
    @ResponseStatus(NOT_FOUND)
    public ResponseErrorBody handleNotFoundException(ResourceNotFoundException e, HttpServletRequest request) {
        return buildErrorBody(NOT_FOUND, e, request);
    }

    @ExceptionHandler({AuthenticationException.class, JwtException.class})
    @ResponseStatus(UNAUTHORIZED)
    public ResponseErrorBody handleUnauthorizedException(Exception e, HttpServletRequest request) {
        return buildErrorBody(UNAUTHORIZED, e, request);
    }

    @ExceptionHandler({AccessDeniedException.class})
    @ResponseStatus(FORBIDDEN)
    public ResponseErrorBody handleAccessDeniedException(Exception e, HttpServletRequest request) {
        return buildErrorBody(FORBIDDEN, e, request);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    @ResponseStatus(BAD_REQUEST)
    public ResponseViolationErrorBody handlePasswordOperationException(InvalidPasswordException e, HttpServletRequest request) {
        ResponseViolationErrorBody result = buildViolationErrorBody(BAD_REQUEST, e, request);
        result.setFieldErrors(Map.of(e.getInvalidFieldName(), e.getMessage()));
        return result;
    }

    @ExceptionHandler
    @ResponseStatus(BAD_REQUEST)
    public ResponseViolationErrorBody handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        ResponseViolationErrorBody result = buildViolationErrorBody(BAD_REQUEST, e, request);

        result.setFieldErrors(e.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(FieldError::getField, ex -> ex.getDefaultMessage() == null
                                ? "Invalid"
                                : ex.getDefaultMessage(),
                        (message, anotherMessage) -> message + " and " + anotherMessage)));
        return result;
    }

    private static ResponseErrorBody buildErrorBody(HttpStatus httpStatus, Exception e, HttpServletRequest request) {
        if (!httpStatus.is5xxServerError())
            log.debug(e.getMessage(), e);
        return ResponseErrorBody.builder()
                .status(httpStatus.value())
                .cause(httpStatus.getReasonPhrase())
                .exception(e.getClass().getName())
                .message(e.getMessage())
                .path(request.getRequestURI())
                .build();
    }

    private static ResponseViolationErrorBody buildViolationErrorBody(HttpStatus httpStatus, Exception e, HttpServletRequest request) {
        log.debug(e.getMessage(), e);
        return ResponseViolationErrorBody.builder()
                .status(httpStatus.value())
                .cause(httpStatus.getReasonPhrase())
                .exception(e.getClass().getName())
                .path(request.getRequestURI())
                .build();
    }
}
