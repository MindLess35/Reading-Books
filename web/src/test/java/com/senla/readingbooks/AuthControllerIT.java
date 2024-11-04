package com.senla.readingbooks;

import com.senla.readingbooks.dto.user.UserCreateDto;
import com.senla.readingbooks.dto.user.UserLoginDto;
import com.senla.readingbooks.exception.base.BadRequestBaseException;
import com.senla.readingbooks.exception.base.ResourceNotFoundException;
import com.senla.readingbooks.exception.security.AuthException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import java.util.stream.Stream;

import static com.senla.readingbooks.controller.user.AuthController.X_REFRESH_TOKEN;
import static com.senla.readingbooks.service.interfaces.auth.AuthService.X_DEVICE_ID;
import static com.senla.readingbooks.service.interfaces.auth.JwtService.BEARER_;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthControllerIT extends IntegrationTestBase {
    private static String accessToken;
    private static String refreshToken;

    @Order(1)
    @Test
    void register_ShouldCreateUser_WhenValidRequest() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getUserCreateDto("existingUsername", "existingEmail@email.com", "passwordA1@")))
                        .header(X_DEVICE_ID, "1"))
                .andExpectAll(
                        status().isCreated(),
                        header().exists(HttpHeaders.AUTHORIZATION),
                        header().exists(HttpHeaders.SET_COOKIE),
                        content().string("")
                )
                .andReturn();

        String accessToken = result.getResponse().getHeader(HttpHeaders.AUTHORIZATION);
        String refreshToken = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertThat(accessToken).isNotBlank().startsWith(BEARER_);
        assertThat(refreshToken).isNotBlank().startsWith(X_REFRESH_TOKEN + "=" + BEARER_).contains("Secure", "HttpOnly");
    }

    @Test
    void register_ShouldReturnBadRequest_WhenDeviceIdHeaderIsEmpty() throws Exception {
        mockMvc.perform(post("/api/v1/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getUserCreateDto("username", "Email@email.com", "passwordA1@")))
                        .header(X_DEVICE_ID, ""))
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()),
                        jsonPath("$.cause").value(HttpStatus.BAD_REQUEST.getReasonPhrase()),
                        jsonPath("$.exception").value(BadRequestBaseException.class.getName())
                );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidAndExistingUserCreateDtos")
    void register_ShouldReturnBadRequest_WhenInvalidOrExistingUsernameOrEmail(UserCreateDto userCreateDto) throws Exception {
        mockMvc.perform(post("/api/v1/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDto))
                        .header(X_DEVICE_ID, "1"))
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()),
                        jsonPath("$.cause").value(HttpStatus.BAD_REQUEST.getReasonPhrase()),
                        jsonPath("$.fieldErrors").exists()
                );
    }

    @Test
    void authenticate_ShouldReturnBadRequest_WhenDeviceIdHeaderIsEmpty() throws Exception {
        mockMvc.perform(post("/api/v1/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getUserLoginDto("username", "passwordA1@")))
                        .header(X_DEVICE_ID, ""))
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()),
                        jsonPath("$.cause").value(HttpStatus.BAD_REQUEST.getReasonPhrase()),
                        jsonPath("$.exception").value(BadRequestBaseException.class.getName())
                );
    }

    @Order(2)
    @Test
    @WithMockUser(username = "existingUser", authorities = "READER")
    void register_ShouldReturnBadRequest_WhenUserAlreadyAuthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getUserCreateDto("anotherUsername", "another@email.com", "passwordA1@")))
                        .header(X_DEVICE_ID, "1"))
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()),
                        jsonPath("$.cause").value(HttpStatus.BAD_REQUEST.getReasonPhrase()),
                        jsonPath("$.exception").value(BadRequestBaseException.class.getName())
                );
    }

    private static Stream<UserCreateDto> provideInvalidAndExistingUserCreateDtos() {
        return Stream.of(
                // Invalid cases
                getUserCreateDto("", "valid@email.com", "passwordA1@"),
                getUserCreateDto("", "", "passwordA1@"),
                getUserCreateDto("", "", ""),
                getUserCreateDto("validUsername", "invalidEmail", "passwordA1@"),
                getUserCreateDto("validUsername", "valid@email.com", "short"),
                getUserCreateDto("validUsername", "valid@email.com", ""),
                getUserCreateDto("validUsername", "", "short"),
                getUserCreateDto(null, "valid@email.com", "passwordA1@"),
                getUserCreateDto("validUsername", null, "passwordA1@"),
                getUserCreateDto(null, null, ""),
                getUserCreateDto(null, null, null),
                getUserCreateDto("validUsername", "valid@email.com", null),
                // Existing user cases
                getUserCreateDto("existingUsername", "valid@email.com", "passwordA1@"),
                getUserCreateDto(null, "valid@email.com", "passwordA1@"),
                getUserCreateDto("validUsername", "existingEmail@email.com", "passwordA1@"),
                getUserCreateDto("validUsername", "", "passwordA1@"),
                getUserCreateDto("existingUsername", "existingEmail@email.com", "passwordA1@"),
                getUserCreateDto("existingUsername", "existingEmail@email.com", ""),
                getUserCreateDto("", "existingEmail@email.com", null),
                getUserCreateDto("existingUsername", null, "passwordA1@")
        );
    }

    private static UserCreateDto getUserCreateDto(String username, String email, String password) {
        return new UserCreateDto(username, email, password);
    }

    @Order(3)
    @Test
    void authenticate_ShouldReturnToken_WhenValidCredentials() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getUserLoginDto("existingUsername", "passwordA1@")))
                        .header(X_DEVICE_ID, "1"))
                .andExpectAll(
                        status().isOk(),
                        header().exists(HttpHeaders.AUTHORIZATION),
                        header().exists(HttpHeaders.SET_COOKIE)
                )
                .andReturn();

        accessToken = result.getResponse().getHeader(HttpHeaders.AUTHORIZATION);
        refreshToken = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertThat(refreshToken).isNotBlank().startsWith(X_REFRESH_TOKEN + "=" + BEARER_).contains("Secure", "HttpOnly");
        refreshToken = refreshToken.substring(refreshToken.indexOf(BEARER_), refreshToken.lastIndexOf("; Path="));

        assertThat(accessToken).isNotBlank().startsWith(BEARER_);
        assertThat(refreshToken).startsWith(BEARER_);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidUserLoginDtos")
    void authenticate_ShouldReturnBadRequest_WhenInvalidCredentials(UserLoginDto userLoginDto) throws Exception {
        mockMvc.perform(post("/api/v1/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userLoginDto))
                        .header(X_DEVICE_ID, "1"))
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()),
                        jsonPath("$.cause").value(HttpStatus.BAD_REQUEST.getReasonPhrase()),
                        jsonPath("$.fieldErrors").exists()
                );
    }

    private static Stream<UserLoginDto> provideInvalidUserLoginDtos() {
        return Stream.of(
                getUserLoginDto("", "passwordA1@"),
                getUserLoginDto("validUsername", null),
                getUserLoginDto(null, "passwordA1@"),
                getUserLoginDto(null, ""),
                getUserLoginDto("", ""),
                getUserLoginDto(null, null),
                getUserLoginDto("username", ""),
                getUserLoginDto("us", "passwordA1@"),
                getUserLoginDto("validUsername", "short")
        );
    }

    @Order(4)
    @Test
    @WithMockUser(username = "existingUser", authorities = "READER")
    void authenticate_ShouldReturnBadRequest_WhenUserAlreadyAuthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getUserLoginDto("username", "passwordA1@")))
                        .header(X_DEVICE_ID, "1"))
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()),
                        jsonPath("$.cause").value(HttpStatus.BAD_REQUEST.getReasonPhrase()),
                        jsonPath("$.exception").value(BadRequestBaseException.class.getName())
                );
    }

    @Order(5)
    @ParameterizedTest
    @MethodSource("provideInvalidPasswordAndNonExistentUsername")
    void authenticate_ShouldReturnBadCredentials_WhenUserNotFoundOrWrongPassword(String username, String password) throws Exception {
        mockMvc.perform(post("/api/v1/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getUserLoginDto(username, password)))
                        .header(X_DEVICE_ID, "1"))
                .andExpectAll(
                        status().isUnauthorized(),
                        jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()),
                        jsonPath("$.cause").value(HttpStatus.UNAUTHORIZED.getReasonPhrase()),
                        jsonPath("$.exception").value(BadCredentialsException.class.getName())
                );
    }

    private static Stream<Arguments> provideInvalidPasswordAndNonExistentUsername() {
        return Stream.of(
                Arguments.of("nonExistentUsername", "Password1!"),
                Arguments.of("existingUsername", "wrongPassword1!")
        );
    }

    private static UserLoginDto getUserLoginDto(String username, String password) {
        return new UserLoginDto(username, password);
    }

    @Order(7)
    @Test
    void refreshToken_ShouldReturnNewTokens_WhenValidRefreshTokenAndHeaders() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/auth/refresh")
                        .header(HttpHeaders.AUTHORIZATION, refreshToken)
                        .header(X_DEVICE_ID, "1"))
                .andExpectAll(
                        status().isOk(),
                        header().exists(HttpHeaders.AUTHORIZATION),
                        header().exists(HttpHeaders.SET_COOKIE)
                )
                .andReturn();

        String accessToken = result.getResponse().getHeader(HttpHeaders.AUTHORIZATION);
        String refreshToken = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);

        assertThat(accessToken).isNotBlank().startsWith(BEARER_);
        assertThat(refreshToken).isNotBlank().startsWith(X_REFRESH_TOKEN + "=" + BEARER_).contains("Secure", "HttpOnly");
        AuthControllerIT.accessToken = accessToken;
    }

    @Order(8)
    @Test
    void refreshToken_ShouldThrowAuthException_WhenTokenIsReused() throws Exception {
        mockMvc.perform(get("/api/v1/auth/refresh")
                        .header(HttpHeaders.AUTHORIZATION, refreshToken)
                        .header(X_DEVICE_ID, "1"))
                .andExpectAll(
                        status().isUnauthorized(),
                        jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()),
                        jsonPath("$.cause").value(HttpStatus.UNAUTHORIZED.getReasonPhrase()),
                        jsonPath("$.exception").value(AuthException.class.getName())
                );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidHeaders")
    void refreshToken_ShouldReturnBadRequest_WhenInvalidHeader(String authHeader) throws Exception {
        mockMvc.perform(get("/api/v1/auth/refresh")
                        .header(HttpHeaders.AUTHORIZATION, authHeader)
                        .header(X_DEVICE_ID, "1"))
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()),
                        jsonPath("$.cause").value(HttpStatus.BAD_REQUEST.getReasonPhrase()),
                        jsonPath("$.exception").value(BadRequestBaseException.class.getName())
                );
    }

    @Order(6)
    @Test
    void refreshToken_ShouldReturnBadRequest_WhenUseAccessToken() throws Exception {
        mockMvc.perform(get("/api/v1/auth/refresh")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .header(X_DEVICE_ID, "1"))
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()),
                        jsonPath("$.cause").value(HttpStatus.BAD_REQUEST.getReasonPhrase())
                );
    }


    @ParameterizedTest
    @MethodSource("provideInvalidTokens")
    void refreshToken_ShouldReturnUnauthorized_WhenInvalidToken(String authHeader) throws Exception {
        mockMvc.perform(get("/api/v1/auth/refresh")
                        .header(HttpHeaders.AUTHORIZATION, authHeader)
                        .header(X_DEVICE_ID, "1"))
                .andExpectAll(
                        status().isUnauthorized(),
                        jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()),
                        jsonPath("$.cause").value(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                );
    }


    private static Stream<String> provideInvalidHeaders() {
        return Stream.of(
                "Bearer",
                "1",
                "null",
                "1Bearer ytj",
                " Bearer 12345432"
        );
    }

    private static Stream<String> provideInvalidTokens() {
        return Stream.of(
                "Bearer ",
                "Bearer 1flnjkrhoijoekwpjro83453.3923.39r8..309472",
                "Bearer null",
                "Bearer 12345432"
        );
    }

    @Test
    void revokeToken_ShouldReturnUnauthorized_WhenUserIsUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/v1/auth/revoke")
                        .header(HttpHeaders.AUTHORIZATION, "accessToken")
                        .header(X_DEVICE_ID, "2"))
                .andExpectAll(
                        status().isUnauthorized(),
                        jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()),
                        jsonPath("$.cause").value(HttpStatus.UNAUTHORIZED.getReasonPhrase()),
                        jsonPath("$.exception").value(AuthenticationCredentialsNotFoundException.class.getName())
                );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidHeaders")
    @WithMockUser(username = "existingUser", authorities = "READER")
    void revokeToken_ShouldReturnBadRequest_WhenHeaderInvalid(String header) throws Exception {
        mockMvc.perform(delete("/api/v1/auth/revoke")
                        .header(HttpHeaders.AUTHORIZATION, header)
                        .header(X_DEVICE_ID, "1"))
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()),
                        jsonPath("$.cause").value(HttpStatus.BAD_REQUEST.getReasonPhrase()),
                        jsonPath("$.exception").value(BadRequestBaseException.class.getName())
                );
    }

    @Order(9)
    @Test
    void revokeToken_ShouldThrowResourceNotFoundException_WhenTokenNotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/auth/revoke")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .header(X_DEVICE_ID, "2"))
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()),
                        jsonPath("$.cause").value(HttpStatus.NOT_FOUND.getReasonPhrase()),
                        jsonPath("$.exception").value(ResourceNotFoundException.class.getName())
                );
    }

}
