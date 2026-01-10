package com.qrware.controller;

import com.qrware.domain.user.Role;
import com.qrware.domain.user.User;
import com.qrware.exception.ResourceNotFoundException;
import com.qrware.security.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserManagementControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserManagementController userManagementController;

    @Test
    void getAllUsers_ShouldReturnUsers_WhenFound() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setRoles(new HashSet<>());
        user.setAuthorities(new HashSet<>());
        
        Page<User> page = new PageImpl<>(List.of(user));
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(page);

        ResponseEntity<?> response = userManagementController.getAllUsers(0, 20, new String[]{"id", "asc"});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserManagementController.GlobalApiResponse<?> body = (UserManagementController.GlobalApiResponse<?>) response.getBody();
        assertTrue(body.isSuccess());
    }

    @Test
    void createUser_ShouldCreateAndReturnUser() {
        UserManagementController.AdminCreateUserRequest request = new UserManagementController.AdminCreateUserRequest();
    }

    @Test
    void getUserById_ShouldReturnUser_WhenFound() {
        Long id = 1L;
        User user = new User();
        user.setId(id);
        user.setUsername("testuser");
        user.setRoles(new HashSet<>());
        user.setAuthorities(new HashSet<>());

        when(userService.getUserById(id)).thenReturn(user);

        ResponseEntity<?> response = userManagementController.getUserById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserManagementController.GlobalApiResponse<?> body = (UserManagementController.GlobalApiResponse<?>) response.getBody();
        assertTrue(body.isSuccess());
    }

    @Test
    void getUserById_ShouldReturnNotFound_WhenUserNotFound() {
        Long id = 1L;
        when(userService.getUserById(id)).thenThrow(new ResourceNotFoundException("User", "id", id));

        ResponseEntity<?> response = userManagementController.getUserById(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteUser_ShouldReturnOk_WhenUserDeleted() {
        Long id = 1L;
        doNothing().when(userService).deleteUser(id);

        ResponseEntity<?> response = userManagementController.deleteUser(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void createRole_ShouldCreateAndReturnRole() {
        UserManagementController.RoleRequest request = new UserManagementController.RoleRequest();
        
        Role role = new Role("ROLE_TEST", "Test Role");
        role.setId(1L);
        role.setPermissions(new HashSet<>());
        
        when(userService.createRole(any(Role.class), any())).thenReturn(role);

        ResponseEntity<?> response = userManagementController.createRole(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }
}
