package com.qrware.security.service;

import com.qrware.domain.user.Permission;
import com.qrware.domain.user.Role;
import com.qrware.domain.user.User;
import com.qrware.exception.ResourceNotFoundException;
import com.qrware.repository.user.PermissionRepository;
import com.qrware.repository.user.RoleRepository;
import com.qrware.repository.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Serwis do zarządzania użytkownikami, rolami i uprawnieniami przez administratora.
 */
@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- Zarządzanie Użytkownikami ---

    /**
     * Pobiera stronę z listą wszystkich użytkowników.
     */
    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable) {
        logger.debug("Pobieranie listy użytkowników, strona: {}, rozmiar: {}", pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findAll(pageable);
    }

    /**
     * Znajduje użytkownika po ID.
     */
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    /**
     * Tworzy nowego użytkownika (przez administratora).
     * UWAGA: Uwzględnia pola active i emailVerified z DTO.
     */
    public User createUser(User newUser, Set<String> roleNames, String rawPassword) {
        if (userRepository.existsByUsername(newUser.getUsername())) {
            throw new IllegalArgumentException("Username jest już zajęty: " + newUser.getUsername());
        }
        if (userRepository.existsByEmail(newUser.getEmail())) {
            throw new IllegalArgumentException("Email jest już zajęty: " + newUser.getEmail());
        }

        newUser.setPassword(passwordEncoder.encode(rawPassword));
        newUser.setRoles(findRolesByName(roleNames));

        // Zastosowanie wartości z DTO (jeśli nie jest nullem), w przeciwnym razie użycie domyślnej z encji User
        if (newUser.getActive() == null) {
            newUser.setActive(true);
        }
        if (newUser.getEmailVerified() == null) {
            newUser.setEmailVerified(false);
        }

        User savedUser = userRepository.save(newUser);
        logger.info("Utworzono nowego użytkownika (przez admina): {}", savedUser.getUsername());
        return savedUser;
    }

    /**
     * Aktualizuje istniejącego użytkownika.
     * UWAGA: Uwzględnia pola active i emailVerified.
     */
    public User updateUser(Long id, User userDetails, Set<String> roleNames) {
        User user = getUserById(id);

        // Sprawdzenie unikalności emaila, jeśli jest zmieniany
        if (!user.getEmail().equals(userDetails.getEmail()) && userRepository.existsByEmail(userDetails.getEmail())) {
            throw new IllegalArgumentException("Email jest już zajęty: " + userDetails.getEmail());
        }

        // Aktualizacja pól
        user.setEmail(userDetails.getEmail());
        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        user.setPhone(userDetails.getPhone());
        user.setActive(userDetails.getActive());
        user.setEmailVerified(userDetails.getEmailVerified()); // DODANE
        user.setRoles(findRolesByName(roleNames));

        User updatedUser = userRepository.save(user);
        logger.info("Zaktualizowano użytkownika (przez admina): {}", updatedUser.getUsername());
        return updatedUser;
    }

    /**
     * Usuwa użytkownika.
     */
    public void deleteUser(Long id) {
        User user = getUserById(id);

        // Zabezpieczenie przed usunięciem samego siebie lub krytycznego konta
        // (Wymaga SecurityUtils, ale dla uproszczenia pomijamy)
        if (user.getUsername().equals("admin")) {
            throw new IllegalArgumentException("Nie można usunąć domyślnego konta administratora.");
        }

        userRepository.delete(user);
        logger.info("Usunięto użytkownika (przez admina): id={}", id);
    }

    /**
     * Blokuje konto użytkownika.
     */
    public void lockUser(Long id) {
        User user = getUserById(id);
        user.lockAccount(99999); // Blokada na bardzo długi czas (ręczne odblokowanie)
        userRepository.save(user);
        logger.warn("Ręcznie zablokowano konto użytkownika (przez admina): {}", user.getUsername());
    }

    /**
     * Odblokowuje konto użytkownika.
     */
    public void unlockUser(Long id) {
        User user = getUserById(id);
        user.unlockAccount();
        userRepository.save(user);
        logger.info("Ręcznie odblokowano konto użytkownika (przez admina): {}", user.getUsername());
    }

    /**
     * Ustawia nowe hasło dla użytkownika (przez administratora).
     */
    public void adminResetPassword(Long id, String newPassword) {
        User user = getUserById(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        logger.info("Administrator zresetował hasło dla użytkownika: {}", user.getUsername());
    }


    // --- Zarządzanie Rolami ---

    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Role getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
    }

    /**
     * Tworzy nową rolę.
     * UWAGA: Uwzględnia pole active z DTO.
     */
    public Role createRole(Role newRole, Set<String> permissionNames) {
        if (roleRepository.existsByName(newRole.getName())) {
            throw new IllegalArgumentException("Rola o nazwie " + newRole.getName() + " już istnieje.");
        }

        // Ustawienie aktywności z DTO (jeśli jest, w przeciwnym razie domyślna z encji)
        if (newRole.getActive() == null) {
            newRole.setActive(true);
        }

        newRole.setPermissions(findPermissionsByName(permissionNames));
        Role savedRole = roleRepository.save(newRole);
        logger.info("Utworzono nową rolę: {}", savedRole.getName());
        return savedRole;
    }

    /**
     * Aktualizuje rolę.
     * UWAGA: Uwzględnia pole active.
     */
    public Role updateRole(Long id, Role roleDetails, Set<String> permissionNames) {
        Role role = getRoleById(id);

        // Sprawdzenie, czy administrator nie próbuje zmienić nazwy na istniejącą
        if (!role.getName().equalsIgnoreCase(roleDetails.getName()) && roleRepository.existsByName(roleDetails.getName())) {
            throw new IllegalArgumentException("Rola o nazwie " + roleDetails.getName() + " już istnieje.");
        }

        role.setName(roleDetails.getName()); // Umożliwienie zmiany nazwy
        role.setDescription(roleDetails.getDescription());
        role.setActive(roleDetails.getActive()); // DODANE
        role.setPermissions(findPermissionsByName(permissionNames));

        Role updatedRole = roleRepository.save(role);
        logger.info("Zaktualizowano rolę: {}", updatedRole.getName());
        return updatedRole;
    }

    public void deleteRole(Long id) {
        Role role = getRoleById(id);
        // TODO: Dodać walidację - nie usuwaj ról, które są przypisane do użytkowników
        if (role.getName().equals("ADMIN") || role.getName().equals("USER")) {
            throw new IllegalArgumentException("Nie można usunąć podstawowych ról systemowych.");
        }

        // Usuń powiązania z użytkownikami przed usunięciem roli
        for (User user : role.getUsers()) {
            user.getRoles().remove(role);
            userRepository.save(user);
        }

        roleRepository.delete(role);
        logger.info("Usunięto rolę: id={}", id);
    }

    // --- Zarządzanie Uprawnieniami ---

    @Transactional(readOnly = true)
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    /**
     * Znajduje uprawnienie po ID.
     */
    @Transactional(readOnly = true)
    public Permission getPermissionById(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", id));
    }

    /**
     * Tworzy nowe uprawnienie.
     */
    public Permission createPermission(Permission newPermission) {
        if (permissionRepository.existsByName(newPermission.getName())) {
            throw new IllegalArgumentException("Uprawnienie o nazwie " + newPermission.getName() + " już istnieje.");
        }

        // Zastosowanie wartości z DTO
        if (newPermission.getActive() == null) {
            newPermission.setActive(true);
        }

        Permission savedPermission = permissionRepository.save(newPermission);
        logger.info("Utworzono nowe uprawnienie: {}", savedPermission.getName());
        return savedPermission;
    }

    /**
     * Aktualizuje istniejące uprawnienie.
     */
    public Permission updatePermission(Long id, Permission permissionDetails) {
        Permission permission = getPermissionById(id);

        // Walidacja nazwy, resource i action (jako że definiują unikalność)
        if (!permission.getName().equalsIgnoreCase(permissionDetails.getName()) && permissionRepository.existsByName(permissionDetails.getName())) {
            throw new IllegalArgumentException("Uprawnienie o nazwie " + permissionDetails.getName() + " już istnieje.");
        }

        permission.setName(permissionDetails.getName());
        permission.setDescription(permissionDetails.getDescription());
        permission.setResource(permissionDetails.getResource());
        permission.setAction(permissionDetails.getAction());
        permission.setActive(permissionDetails.getActive());

        Permission updatedPermission = permissionRepository.save(permission);
        logger.info("Zaktualizowano uprawnienie: {}", updatedPermission.getName());
        return updatedPermission;
    }

    /**
     * Usuwa uprawnienie.
     */
    public void deletePermission(Long id) {
        Permission permission = getPermissionById(id);

        // Opcjonalnie: sprawdzenie, czy uprawnienie jest używane przez role
        if (!permission.getRoles().isEmpty()) {
            throw new IllegalArgumentException("Nie można usunąć uprawnienia, jest przypisane do ról: " + permission.getRoles().stream().map(Role::getName).collect(Collectors.joining(", ")));
        }

        permissionRepository.delete(permission);
        logger.info("Usunięto uprawnienie: id={}", id);
    }


    // --- Metody pomocnicze ---

    private Set<Role> findRolesByName(Set<String> roleNames) {
        return roleNames.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName)))
                .collect(Collectors.toSet());
    }

    private Set<Permission> findPermissionsByName(Set<String> permissionNames) {
        return permissionNames.stream()
                .map(permName -> permissionRepository.findByName(permName)
                        .orElseThrow(() -> new ResourceNotFoundException("Permission", "name", permName)))
                .collect(Collectors.toSet());
    }
}