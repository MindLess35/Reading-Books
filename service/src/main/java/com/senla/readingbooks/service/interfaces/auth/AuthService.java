package com.senla.readingbooks.service.interfaces.auth;

import com.senla.readingbooks.dto.auth.JwtResponseDto;
import com.senla.readingbooks.dto.user.UserCreateDto;
import com.senla.readingbooks.dto.user.UserLoginDto;

public interface AuthService {
    String X_DEVICE_ID = "X-Device-Id";

    JwtResponseDto createUser(UserCreateDto userCreateDto, String deviceId);

    JwtResponseDto login(UserLoginDto userLoginDto, String deviceId);

    JwtResponseDto refreshToken(String refreshToken, String deviceId);

    void revokeToken(String authHeader, String deviceId);

}
