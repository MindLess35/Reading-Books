package com.senla.readingbooks.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.senla.readingbooks.exception.response.ResponseErrorBody;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {

        log.debug("Access denied: {}", ex.getMessage(), ex);
        response.setStatus(FORBIDDEN.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ResponseErrorBody errorBody = ResponseErrorBody.builder()
                .status(FORBIDDEN.value())
                .cause(FORBIDDEN.getReasonPhrase())
                .exception(ex.getClass().getName())
                .message("There are not enough authority to access this resource or perform this action: " + ex.getMessage())
                .path(request.getRequestURI())
                .build();
        response.getWriter().write(objectMapper.writeValueAsString(errorBody));
    }
}
