package com.qrware.controller;

import com.qrware.domain.user.Permission;
import com.qrware.domain.user.Role;
import com.qrware.domain.user.User;
import com.qrware.exception.ResourceNotFoundException;
import com.qrware.security.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant; // ZMIANA: Potrzebne do timestampu
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Kontroler do zarządzania Użytkownikami, Rolami i Uprawnieniami.
 * Dostępny tylko dla administratorów.
 */
@RestController
@RequestMapping("/api")
@PreAuthorize("hasAuthority('ADMIN_FULL')")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserManagementController {

    private static final Logger logger = LoggerFactory.getLogger(UserManagementController.class);

    @Autowired
    private UserService userService;

    // ZMIANA: Definicja generycznej klasy odpowiedzi, pasującej do ApiResponse.kt w Androidzie
    public static class GlobalApiResponse<T> {
        private final boolean success;
        private final String message;
        private final T data;
        private final String timestamp;

        // Konstruktor dla sukcesu (z danymi)
        public GlobalApiResponse(boolean success, String message, T data) {
            this.success = success;
            this.message = message;
            this.data = data;
            this.timestamp = Instant.now().toString(); // Automatycznie dodaje timestamp
        }

        // Konstruktor dla błędu (bez danych)
        public GlobalApiResponse(boolean success, String message) {
            this(success, message, null);
        }

        // Gettery są niezbędne dla Jacksona (serializacja JSON)
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public T getData() { return data; }
        public String getTimestamp() { return timestamp; }
    }

    // ZMIANA: Metoda pomocnicza zwraca nowy typ GlobalApiResponse
    private <T> ResponseEntity<GlobalApiResponse<T>> buildSuccessResponse(T data, String message, HttpStatus status) {
        return ResponseEntity.status(status).body(
                new GlobalApiResponse<>(true, message, data) // success = true
        );
    }

    // ZMIANA: Metoda pomocnicza zwraca nowy typ GlobalApiResponse
    private ResponseEntity<GlobalApiResponse<Object>> buildErrorResponse(String message, HttpStatus status) {
        logger.warn("Błąd zarządzania użytkownikami: {} (Status: {})", message, status);
        return ResponseEntity.status(status).body(
                new GlobalApiResponse<>(false, message) // success = false
        );
    }

    // =================================================================================================================
    // DTOs (Data Transfer Objects) - Bez zmian
    // =================================================================================================================

    /**
     * DTO: Odpowiedź ze szczegółowymi danymi użytkownika dla administratora.
     * UWAGA: Dodano active, emailVerified.
     */
    public static class AdminUserResponse {
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String phone;
        private Boolean active; // DODANE
        private Boolean emailVerified; // DODANE
        private LocalDateTime lastLogin;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private boolean accountNonLocked;
        private List<String> roles;
        private Set<String> permissions;

        public AdminUserResponse(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.email = user.getEmail();
            this.firstName = user.getFirstName();
            this.lastName = user.getLastName();
            this.phone = user.getPhone();
            this.active = user.getActive();
            this.emailVerified = user.getEmailVerified();
            this.lastLogin = user.getLastLogin();
            this.createdAt = user.getCreatedAt();
            this.updatedAt = user.getUpdatedAt();
            this.accountNonLocked = user.isAccountNonLocked();
            this.roles = user.getRoles().stream().map(Role::getName).sorted().collect(Collectors.toList());
            this.permissions = user.getAuthorities().stream().map(Object::toString).collect(Collectors.toSet());
        }

        // Gettery (usunięto z poprzedniej wersji, dodaję z powrotem dla kompletności DTO)
        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getPhone() { return phone; }
        public Boolean getActive() { return active; }
        public Boolean getEmailVerified() { return emailVerified; }
        public LocalDateTime getLastLogin() { return lastLogin; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public boolean isAccountNonLocked() { return accountNonLocked; }
        public List<String> getRoles() { return roles; }
        public Set<String> getPermissions() { return permissions; }
    }

    /**
     * DTO: Żądanie utworzenia użytkownika przez administratora.
     * UWAGA: Dodano active i emailVerified.
     */
    public static class AdminCreateUserRequest {
        @NotBlank @Size(min = 3, max = 50) private String username;
        @NotBlank @jakarta.validation.constraints.Email private String email;
        @NotBlank @Size(min = 8, max = 100) private String password;
        @NotBlank private String firstName;
        @NotBlank private String lastName;
        private String phone;
        @jakarta.validation.constraints.NotEmpty private Set<String> roles;

        private Boolean active; // DODANE
        private Boolean emailVerified; // DODANE

        // Gettery
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getPhone() { return phone; }
        public Set<String> getRoles() { return roles; }
        public Boolean getActive() { return active; }
        public Boolean getEmailVerified() { return emailVerified; }
    }

    /**
     * DTO: Żądanie aktualizacji użytkownika przez administratora.
     * UWAGA: Dodano emailVerified.
     */
    public static class UpdateUserRequest {
        @jakarta.validation.constraints.Email private String email;
        private String firstName;
        private String lastName;
        private String phone;
        @jakarta.validation.constraints.NotNull private Boolean active;
        @jakarta.validation.constraints.NotNull private Boolean emailVerified; // DODANE
        @jakarta.validation.constraints.NotEmpty private Set<String> roles;

        // Gettery
        public String getEmail() { return email; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getPhone() { return phone; }
        public Boolean getActive() { return active; }
        public Set<String> getRoles() { return roles; }
        public Boolean getEmailVerified() { return emailVerified; }
    }

    /**
     * DTO: Żądanie resetu hasła przez administratora.
     */
    public static class AdminResetPasswordRequest {
        @NotBlank @Size(min = 8, max = 100) private String newPassword;
        public String getNewPassword() { return newPassword; }
    }

    /**
     * DTO: Odpowiedź dla Roli.
     * UWAGA: Dodano active.
     */
    public static class RoleResponse {
        private Long id;
        private String name;
        private String description;
        private Boolean active; // DODANE
        private List<String> permissions;

        public RoleResponse(Role role) {
            this.id = role.getId();
            this.name = role.getName();
            this.description = role.getDescription();
            this.active = role.getActive(); // DODANE
            this.permissions = role.getPermissions().stream()
                    .map(Permission::getName)
                    .sorted()
                    .collect(Collectors.toList());
        }
        // Gettery
        public Long getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public Boolean getActive() { return active; }
        public List<String> getPermissions() { return permissions; }
    }

    /**
     * DTO: Żądanie utworzenia/aktualizacji Roli.
     * UWAGA: Dodano active.
     */
    public static class RoleRequest {
        @NotBlank @Size(max = 50) private String name;
        @Size(max = 255) private String description;
        private Set<String> permissions;
        @jakarta.validation.constraints.NotNull private Boolean active; // DODANE

        // Gettery
        public String getName() { return name; }
        public String getDescription() { return description; }
        public Set<String> getPermissions() { return permissions; }
        public Boolean getActive() { return active; }
    }

    /**
     * DTO: Odpowiedź dla Uprawnienia.
     * UWAGA: Dodano active.
     */
    public static class PermissionResponse {
        private Long id;
        private String name;
        private String description;
        private String resource;
        private String action;
        private Boolean active; // DODANE

        public PermissionResponse(Permission permission) {
            this.id = permission.getId();
            this.name = permission.getName();
            this.description = permission.getDescription();
            this.resource = permission.getResource();
            this.action = permission.getAction();
            this.active = permission.getActive(); // DODANE
        }
        // Gettery
        public Long getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getResource() { return resource; }
        public String getAction() { return action; }
        public Boolean getActive() { return active; }
    }

    /**
     * DTO: Żądanie utworzenia/aktualizacji Uprawnienia.
     * UWAGA: Nowe DTO.
     */
    public static class PermissionRequest {
        private Long id;
        @NotBlank @Size(max = 100) private String name;
        @Size(max = 255) private String description;
        @NotBlank @Size(max = 50) private String resource;
        @NotBlank @Size(max = 20) private String action;
        @jakarta.validation.constraints.NotNull private Boolean active; // DODANE

        // Gettery
        public Long getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getResource() { return resource; }
        public String getAction() { return action; }
        public Boolean getActive() { return active; }
    }


    // --- ENDPOINTY UŻYTKOWNIKÓW ---
    // ZMIANA: Typy zwracane przez endpointy (ResponseEntity<?>) pozostają bez zmian,
    // ponieważ GlobalApiResponse pasuje do ?, ale metody pomocnicze (build...Response) zostały zmienione.

    /**
     * Pobiera listę wszystkich użytkowników (stronicowanie).
     */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {
        try {
            Sort sortOrder = Sort.by(sort[0]).ascending();
            if (sort.length > 1 && sort[1].equalsIgnoreCase("desc")) {
                sortOrder = Sort.by(sort[0]).descending();
            }
            Pageable pageable = PageRequest.of(page, size, sortOrder);
            Page<User> userPage = userService.getAllUsers(pageable); // Zgodne z UserService

            // Mapowanie na DTO
            Page<AdminUserResponse> responsePage = userPage.map(AdminUserResponse::new);

            return buildSuccessResponse(responsePage, "Pobrano listę użytkowników", HttpStatus.OK);
        } catch (Exception ex) {
            return buildErrorResponse("Błąd podczas pobierania użytkowników: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Wyszukuje użytkowników na podstawie zapytania.
     */
    @GetMapping("/users/search")
    public ResponseEntity<?> searchUsers(
            @RequestParam("query") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {
        try {
            Sort sortOrder = Sort.by(sort[0]).ascending();
            if (sort.length > 1 && sort[1].equalsIgnoreCase("desc")) {
                sortOrder = Sort.by(sort[0]).descending();
            }
            Pageable pageable = PageRequest.of(page, size, sortOrder);
            Page<User> userPage = userService.searchUsers(query, pageable);

            // Mapowanie na DTO
            Page<AdminUserResponse> responsePage = userPage.map(AdminUserResponse::new);

            return buildSuccessResponse(responsePage, "Znaleziono użytkowników dla zapytania: " + query, HttpStatus.OK);
        } catch (Exception ex) {
            return buildErrorResponse("Błąd podczas wyszukiwania użytkowników: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Tworzy nowego użytkownika (przez administratora).
     */
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@Valid @RequestBody AdminCreateUserRequest createRequest) {
        try {
            // Walidacja hasła
            if (createRequest.getPassword() == null || createRequest.getPassword().length() < 8) {
                return buildErrorResponse("Hasło musi mieć co najmniej 8 znaków", HttpStatus.BAD_REQUEST);
            }

            User newUser = new User();
            newUser.setUsername(createRequest.getUsername());
            newUser.setEmail(createRequest.getEmail());
            newUser.setFirstName(createRequest.getFirstName());
            newUser.setLastName(createRequest.getLastName());
            newUser.setPhone(createRequest.getPhone());
            newUser.setActive(createRequest.getActive()); // Ustawienie pola z DTO
            newUser.setEmailVerified(createRequest.getEmailVerified()); // Ustawienie pola z DTO

            User savedUser = userService.createUser(newUser, createRequest.getRoles(), createRequest.getPassword()); // Zgodne z UserService
            return buildSuccessResponse(new AdminUserResponse(savedUser), "Utworzono użytkownika", HttpStatus.CREATED);

        } catch (IllegalArgumentException ex) {
            return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            return buildErrorResponse("Błąd podczas tworzenia użytkownika: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Pobiera szczegóły konkretnego użytkownika.
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id); // Zgodne z UserService
            return buildSuccessResponse(new AdminUserResponse(user), "Pobrano użytkownika", HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            return buildErrorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Aktualizuje dane użytkownika.
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest updateRequest) {
        try {
            User userDetails = new User();
            userDetails.setEmail(updateRequest.getEmail());
            userDetails.setFirstName(updateRequest.getFirstName());
            userDetails.setLastName(updateRequest.getLastName());
            userDetails.setPhone(updateRequest.getPhone());
            userDetails.setActive(updateRequest.getActive());
            userDetails.setEmailVerified(updateRequest.getEmailVerified()); // Ustawienie pola z DTO

            User updatedUser = userService.updateUser(id, userDetails, updateRequest.getRoles()); // Zgodne z UserService
            return buildSuccessResponse(new AdminUserResponse(updatedUser), "Zaktualizowano użytkownika", HttpStatus.OK);

        } catch (ResourceNotFoundException ex) {
            return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException ex) {
            return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            return buildErrorResponse("Błąd podczas aktualizacji użytkownika: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Usuwa użytkownika.
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id); // Zgodne z UserService
            return buildSuccessResponse(null, "Usunięto użytkownika", HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException ex) {
            return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            return buildErrorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Resetuje hasło użytkownika (przez administratora).
     */
    @PostMapping("/users/{id}/reset-password")
    public ResponseEntity<?> adminResetPassword(@PathVariable Long id, @Valid @RequestBody AdminResetPasswordRequest request) {
        try {
            userService.adminResetPassword(id, request.getNewPassword()); // Zgodne z UserService
            return buildSuccessResponse(null, "Hasło zostało zresetowane", HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            return buildErrorResponse("Błąd resetowania hasła: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Zmienia hasło użytkownika (używając nowej nazwy z poprzedniego kroku, ale zostawiam starszą /reset-password, ponieważ występuje w pliku).
     */
    @PatchMapping("/users/{id}/password") // Dodany alternatywny endpoint zgodny z REST
    public ResponseEntity<?> changePassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String newPassword = request.get("newPassword");
            if (newPassword == null || newPassword.length() < 8) {
                return buildErrorResponse("Nowe hasło musi mieć co najmniej 8 znaków", HttpStatus.BAD_REQUEST);
            }
            userService.adminResetPassword(id, newPassword);
            return buildSuccessResponse(null, "Zmieniono hasło użytkownika", HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            return buildErrorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Blokuje konto użytkownika.
     */
    @PostMapping("/users/{id}/lock")
    public ResponseEntity<?> lockUser(@PathVariable Long id) {
        try {
            userService.lockUser(id); // Zgodne z UserService
            return buildSuccessResponse(null, "Konto użytkownika zablokowane", HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            return buildErrorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Odblokowuje konto użytkownika.
     */
    @PostMapping("/users/{id}/unlock")
    public ResponseEntity<?> unlockUser(@PathVariable Long id) {
        try {
            userService.unlockUser(id); // Zgodne z UserService
            return buildSuccessResponse(null, "Konto użytkownika odblokowane", HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            return buildErrorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // --- ENDPOINTY RÓL ---

    /**
     * Pobiera listę wszystkich ról.
     */
    @GetMapping("/roles")
    public ResponseEntity<?> getAllRoles() {
        try {
            List<RoleResponse> roles = userService.getAllRoles().stream() // Zgodne z UserService
                    .map(RoleResponse::new)
                    .collect(Collectors.toList());
            return buildSuccessResponse(roles, "Pobrano role", HttpStatus.OK);
        } catch (Exception ex) {
            return buildErrorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Pobiera szczegóły roli.
     */
    @GetMapping("/roles/{id}")
    public ResponseEntity<?> getRoleById(@PathVariable Long id) {
        try {
            Role role = userService.getRoleById(id); // Zgodne z UserService
            return buildSuccessResponse(new RoleResponse(role), "Pobrano rolę", HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            return buildErrorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Tworzy nową rolę.
     */
    @PostMapping("/roles")
    public ResponseEntity<?> createRole(@Valid @RequestBody RoleRequest roleRequest) {
        try {
            Role newRole = new Role(roleRequest.getName(), roleRequest.getDescription());
            newRole.setActive(roleRequest.getActive()); // Ustawienie pola z DTO

            Role savedRole = userService.createRole(newRole, roleRequest.getPermissions()); // Zgodne z UserService
            return buildSuccessResponse(new RoleResponse(savedRole), "Utworzono rolę", HttpStatus.CREATED);
        } catch (IllegalArgumentException ex) {
            return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            return buildErrorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Aktualizuje rolę (opis, aktywność i uprawnienia).
     */
    @PutMapping("/roles/{id}")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @Valid @RequestBody RoleRequest roleRequest) {
        try {
            Role roleDetails = new Role(roleRequest.getName(), roleRequest.getDescription());
            roleDetails.setActive(roleRequest.getActive()); // Ustawienie pola z DTO

            Role updatedRole = userService.updateRole(id, roleDetails, roleRequest.getPermissions()); // Zgodne z UserService
            return buildSuccessResponse(new RoleResponse(updatedRole), "Zaktualizowano rolę", HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException ex) {
            return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            return buildErrorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Usuwa rolę.
     */
    @DeleteMapping("/roles/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable Long id) {
        try {
            userService.deleteRole(id); // Zgodne z UserService
            return buildSuccessResponse(null, "Usunięto rolę", HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException ex) {
            return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            return buildErrorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // --- ENDPOINTY UPRAWNIEŃ ---

    /**
     * Pobiera listę wszystkich dostępnych uprawnień.
     */
    @GetMapping("/permissions")
    public ResponseEntity<?> getAllPermissions() {
        try {
            List<PermissionResponse> permissions = userService.getAllPermissions().stream() // Zgodne z UserService
                    .map(PermissionResponse::new)
                    .collect(Collectors.toList());
            return buildSuccessResponse(permissions, "Pobrano uprawnienia", HttpStatus.OK);
        } catch (Exception ex) {
            return buildErrorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Pobiera szczegóły uprawnienia.
     */
    @GetMapping("/permissions/{id}")
    public ResponseEntity<?> getPermissionById(@PathVariable Long id) {
        try {
            Permission permission = userService.getPermissionById(id);
            return buildSuccessResponse(new PermissionResponse(permission), "Pobrano uprawnienie", HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            return buildErrorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Tworzy nowe uprawnienie.
     */
    @PostMapping("/permissions")
    public ResponseEntity<?> createPermission(@Valid @RequestBody PermissionRequest request) {
        try {
            Permission newPermission = new Permission(
                    request.getName(),
                    request.getDescription(),
                    request.getResource(),
                    request.getAction()
            );
            newPermission.setActive(request.getActive()); // Ustawienie pola z DTO

            Permission savedPermission = userService.createPermission(newPermission);

            return buildSuccessResponse(new PermissionResponse(savedPermission), "Utworzono uprawnienie", HttpStatus.CREATED);
        } catch (IllegalArgumentException ex) {
            return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            return buildErrorResponse("Błąd tworzenia uprawnienia: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PutMapping("/permissions/{id}")
    public ResponseEntity<?> updatePermission(@PathVariable Long id, @Valid @RequestBody PermissionRequest request) {
        try {
            Permission permissionDetails = new Permission(
                    request.getName(),
                    request.getDescription(),
                    request.getResource(),
                    request.getAction()
            );
            permissionDetails.setActive(request.getActive()); // Ustawienie pola z DTO

            Permission updatedPermission = userService.updatePermission(id, permissionDetails);

            return buildSuccessResponse(new PermissionResponse(updatedPermission), "Zaktualizowano uprawnienie", HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException ex) {
            return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            return buildErrorResponse("Błąd aktualizacji uprawnienia: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Usuwa uprawnienie.
     */
    @DeleteMapping("/permissions/{id}")
    public ResponseEntity<?> deletePermission(@PathVariable Long id) {
        try {
            userService.deletePermission(id);
            return buildSuccessResponse(null, "Usunięto uprawnienie", HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException ex) {
            return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            return buildErrorResponse("Błąd usuwania uprawnienia: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}