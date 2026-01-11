package com.qrware.integration;

import com.qrware.domain.user.Role;
import com.qrware.domain.user.User;
import com.qrware.repository.user.RoleRepository;
import com.qrware.repository.user.UserRepository;
import com.qrware.security.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User adminFullUser;
    private User regularUser;

    @BeforeEach
    void setUp() {
        Role adminRole = roleRepository.findByName("ADMIN")
            .orElseGet(() -> roleRepository.save(new Role("ADMIN", "Admin Role")));

        Role managerRole = roleRepository.findByName("WAREHOUSE_MANAGER")
            .orElseGet(() -> roleRepository.save(new Role("WAREHOUSE_MANAGER", "Warehouse Manager Role")));

        adminFullUser = userRepository.findByUsername("adminfull").orElseGet(() -> {
            User u = new User();
            u.setUsername("adminfull");
            u.setEmail("adminfull@qrware.com");
            u.setPassword("password123");
            u.setFirstName("Admin");
            u.setLastName("Full");
            u.setRoles(new HashSet<>(Collections.singletonList(adminRole)));
            return userRepository.save(u);
        });

        regularUser = userRepository.findByUsername("regular").orElseGet(() -> {
            User u = new User();
            u.setUsername("regular");
            u.setEmail("regular@qrware.com");
            u.setPassword("password123");
            u.setFirstName("Regular");
            u.setLastName("User");
            u.setRoles(new HashSet<>(Collections.singletonList(managerRole)));
            return userRepository.save(u);
        });
    }

    @Test
    void health_ShouldBePublic() throws Exception {
        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk());
    }

    @Test
    void protectedEndpoint_ShouldReturn401_WhenNoAuthentication() throws Exception {
        mockMvc.perform(get("/api/orders")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    @WithMockUser(username = "regular", authorities = {"ORDER_READ"})
    void adminEndpoint_ShouldReturn403_WhenNoAdminFullAuthority() throws Exception {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(Optional.of(regularUser));
            securityUtils.when(SecurityUtils::getCurrentUsername).thenReturn(Optional.of(regularUser.getUsername()));

            mockMvc.perform(get("/api/users")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
        }
    }

    @Test
    @WithMockUser(username = "adminfull", authorities = {"ADMIN_FULL"})
    void adminEndpoint_ShouldReturn200_WhenHasAdminFullAuthority() throws Exception {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(Optional.of(adminFullUser));
            securityUtils.when(SecurityUtils::getCurrentUsername).thenReturn(Optional.of(adminFullUser.getUsername()));

            mockMvc.perform(get("/api/users")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        }
    }
}
