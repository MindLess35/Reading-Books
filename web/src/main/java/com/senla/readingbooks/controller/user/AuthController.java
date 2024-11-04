package com.senla.readingbooks.controller.user;

import com.senla.readingbooks.dto.auth.JwtResponseDto;
import com.senla.readingbooks.dto.user.UserCreateDto;
import com.senla.readingbooks.dto.user.UserLoginDto;
import com.senla.readingbooks.property.JwtProperty;
import com.senla.readingbooks.service.interfaces.auth.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.senla.readingbooks.service.interfaces.auth.AuthService.X_DEVICE_ID;
import static com.senla.readingbooks.service.interfaces.auth.JwtService.BEARER_;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.SET_COOKIE;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@Tag(name = "Authentication Controller", description = "Controller for registration, login, token revocation and its refreshing")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtProperty jwtProperty;
    public static final String X_REFRESH_TOKEN = "X-Refresh-Token";

    @PostMapping("/sign-up")
    public ResponseEntity<String> register(@RequestBody @Validated UserCreateDto userCreateDto,
                                           @RequestHeader(X_DEVICE_ID) String deviceId) {
        return buildResponseEntity(CREATED, authService.createUser(userCreateDto, deviceId));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<String> authenticate(@RequestBody @Validated UserLoginDto userLoginDto,
                                               @RequestHeader(X_DEVICE_ID) String deviceId) {
        return buildResponseEntity(OK, authService.login(userLoginDto, deviceId));
    }

    @GetMapping("/refresh")
    public ResponseEntity<String> refreshToken(@RequestHeader(AUTHORIZATION) String authHeader,
                                               @RequestHeader(X_DEVICE_ID) String deviceId) {
        return buildResponseEntity(OK, authService.refreshToken(authHeader, deviceId));
    }

    @DeleteMapping("/revoke")
    public ResponseEntity<HttpStatus> revokeToken(@RequestHeader(AUTHORIZATION) String authHeader,
                                                  @RequestHeader(X_DEVICE_ID) String deviceId) {
        authService.revokeToken(authHeader, deviceId);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<String> buildResponseEntity(HttpStatus status, JwtResponseDto dto) {
        String cookie = String.format(X_REFRESH_TOKEN + "=%s; HttpOnly; Secure; Path=/; Max-Age=%d",
                BEARER_ + dto.refreshToken(), jwtProperty.getRefreshExpiration() / 1000);

        return ResponseEntity
                .status(status)
                .header(SET_COOKIE, cookie)
                .header(AUTHORIZATION, BEARER_ + dto.accessToken())
                .build();
    }

}

