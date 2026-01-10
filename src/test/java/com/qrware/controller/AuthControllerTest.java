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
        request.setUsernameOrEmail("testuser");
        request.setPassword("password");

        AuthenticationResponse authResponse = new AuthenticationResponse(
            "token", "refresh", "testuser", 1L, null
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
            "token", "refresh", "newuser", 1L, null
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
}
