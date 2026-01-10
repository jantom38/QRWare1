package com.qrware.controller;

import com.qrware.security.service.AuthenticationService;
import com.qrware.security.service.AuthenticationService.AuthenticationResponse;
import com.qrware.security.service.AuthenticationService.LoginRequest;
import com.qrware.security.service.AuthenticationService.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthController authController;

    @Test
    void login_ShouldReturnOk_WhenLoginSuccessful() {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("user");
        request.setPassword("password");

        AuthenticationResponse authResponse = new AuthenticationResponse(
            "token",
            "refresh",
            3600L,
            1L,
            "testuser",
            "test@example.com",
            "Test User",
            Collections.singletonList("USER")
        );

        when(authenticationService.login(any(LoginRequest.class))).thenReturn(authResponse);

        ResponseEntity<?> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        AuthController.ApiResponse<?> body = (AuthController.ApiResponse<?>) response.getBody();
        assertTrue(body.isSuccess());
        assertEquals(authResponse, body.getData());
    }

    @Test
    void login_ShouldReturnUnauthorized_WhenLoginFails() {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("testuser");
        request.setPassword("wrongpassword");

        when(authenticationService.login(any(LoginRequest.class))).thenThrow(new RuntimeException("Bad credentials"));

        ResponseEntity<?> response = authController.login(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        AuthController.ApiResponse<?> body = (AuthController.ApiResponse<?>) response.getBody();
        assertFalse(body.isSuccess());
    }

    @Test
    void register_ShouldReturnCreated_WhenRegistrationSuccessful() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("new@example.com");
        request.setPassword("password");

        AuthenticationResponse authResponse = new AuthenticationResponse(
            "token",           // accessToken
            "refresh",         // refreshToken
            3600L,             // expiresIn
            1L,                // userId
            "newuser",         // username
            "new@example.com", // email
            "New User",        // fullName
            Collections.singletonList("USER") // roles
        );

        when(authenticationService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        ResponseEntity<?> response = authController.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        AuthController.ApiResponse<?> body = (AuthController.ApiResponse<?>) response.getBody();
        assertTrue(body.isSuccess());
        assertEquals(authResponse, body.getData());
    }

    @Test
    void register_ShouldReturnBadRequest_WhenRegistrationFails() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");

        when(authenticationService.register(any(RegisterRequest.class))).thenThrow(new RuntimeException("Username taken"));

        ResponseEntity<?> response = authController.register(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        AuthController.ApiResponse<?> body = (AuthController.ApiResponse<?>) response.getBody();
        assertFalse(body.isSuccess());
    }

    @Test
    void login_ShouldReturnUnauthorized_WhenServiceReturnsNull() {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("user");
        request.setPassword("password");

        when(authenticationService.login(any(LoginRequest.class))).thenReturn(null);

        ResponseEntity<?> response = authController.login(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        AuthController.ApiResponse<?> body = (AuthController.ApiResponse<?>) response.getBody();
        assertFalse(body.isSuccess());
        assertEquals("Login failed: Invalid credentials", body.getMessage());
    }

    // ==================== REFRESH TOKEN TESTS ====================

    @Test
    void refreshToken_ShouldReturnOk_WhenTokenValid() {
        AuthenticationService.RefreshTokenRequest request = new AuthenticationService.RefreshTokenRequest();
        request.setRefreshToken("valid-refresh-token");

        AuthenticationResponse authResponse = new AuthenticationResponse(
            "new-token",
            "new-refresh",
            3600L,
            1L,
            "testuser",
            "test@example.com",
            "Test User",
            Collections.singletonList("USER")
        );

        when(authenticationService.refreshToken(any(AuthenticationService.RefreshTokenRequest.class))).thenReturn(authResponse);

        ResponseEntity<?> response = authController.refreshToken(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        AuthController.ApiResponse<?> body = (AuthController.ApiResponse<?>) response.getBody();
        assertTrue(body.isSuccess());
        assertEquals("Token refreshed successfully", body.getMessage());
    }

    @Test
    void refreshToken_ShouldReturnUnauthorized_WhenTokenExpired() {
        AuthenticationService.RefreshTokenRequest request = new AuthenticationService.RefreshTokenRequest();
        request.setRefreshToken("expired-token");

        when(authenticationService.refreshToken(any(AuthenticationService.RefreshTokenRequest.class)))
            .thenThrow(new RuntimeException("Token expired"));

        ResponseEntity<?> response = authController.refreshToken(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        AuthController.ApiResponse<?> body = (AuthController.ApiResponse<?>) response.getBody();
        assertFalse(body.isSuccess());
    }

    @Test
    void refreshToken_ShouldReturnUnauthorized_WhenServiceReturnsNull() {
        AuthenticationService.RefreshTokenRequest request = new AuthenticationService.RefreshTokenRequest();
        request.setRefreshToken("invalid-token");

        when(authenticationService.refreshToken(any(AuthenticationService.RefreshTokenRequest.class))).thenReturn(null);

        ResponseEntity<?> response = authController.refreshToken(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        AuthController.ApiResponse<?> body = (AuthController.ApiResponse<?>) response.getBody();
        assertFalse(body.isSuccess());
        assertEquals("Token refresh failed", body.getMessage());
    }

    // ==================== REGISTER EDGE CASES ====================

    @Test
    void register_ShouldReturnBadRequest_WhenServiceReturnsNull() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("new@example.com");
        request.setPassword("password");

        when(authenticationService.register(any(RegisterRequest.class))).thenReturn(null);

        ResponseEntity<?> response = authController.register(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        AuthController.ApiResponse<?> body = (AuthController.ApiResponse<?>) response.getBody();
        assertFalse(body.isSuccess());
        assertEquals("Registration failed", body.getMessage());
    }

    @Test
    void register_ShouldReturnBadRequest_WhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("existing@example.com");
        request.setPassword("password");

        when(authenticationService.register(any(RegisterRequest.class)))
            .thenThrow(new RuntimeException("Email already registered"));

        ResponseEntity<?> response = authController.register(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        AuthController.ApiResponse<?> body = (AuthController.ApiResponse<?>) response.getBody();
        assertFalse(body.isSuccess());
        assertTrue(body.getMessage().contains("Email already registered"));
    }
}
