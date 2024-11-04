package com.senla.readingbooks.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.senla.readingbooks.exception.response.ResponseErrorBody;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException ex) throws IOException {

        log.debug("Authentication error: {}", ex.getMessage(), ex);
        response.setStatus(UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ResponseErrorBody errorBody = ResponseErrorBody.builder()
                .status(UNAUTHORIZED.value())
                .cause(UNAUTHORIZED.getReasonPhrase())
                .exception(ex.getClass().getName())
                .message("To access this resource, you need to authenticate: " + ex.getMessage())
                .path(request.getRequestURI())
                .build();
        response.getWriter().write(objectMapper.writeValueAsString(errorBody));
    }
}