package com.qrware.repository.user;

import com.qrware.domain.user.Permission;
import com.qrware.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository interface for Permission entity operations
 */
@Repository
public interface PermissionRepository extends BaseRepository<Permission> {

    /**
     * Find permission by name
     */
    Optional<Permission> findByName(String name);

    /**
     * Find permission by name ignoring case
     */
    Optional<Permission> findByNameIgnoreCase(String name);

    /**
     * Check if permission exists by name
     */
    boolean existsByName(String name);

    /**
     * Check if permission name exists excluding current permission
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Permission p WHERE p.name = :name AND p.id != :permissionId")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("permissionId") Long permissionId);

    /**
     * Find permission by resource and action
     */
    Optional<Permission> findByResourceAndAction(String resource, String action);

    /**
     * Check if permission exists by resource and action
     */
    boolean existsByResourceAndAction(String resource, String action);

    /**
     * Find all active permissions
     */
    List<Permission> findByActiveTrue();

    /**
     * Find all inactive permissions
     */
    List<Permission> findByActiveFalse();

    /**
     * Find permissions by active status ordered by name
     */
    List<Permission> findByActiveOrderByName(Boolean active);

    /**
     * Find permissions by resource
     */
    List<Permission> findByResource(String resource);

    /**
     * Find permissions by action
     */
    List<Permission> findByAction(String action);

    /**
     * Find permissions by resource ordered by action
     */
    List<Permission> findByResourceOrderByAction(String resource);

    /**
     * Find permissions by action ordered by resource
     */
    List<Permission> findByActionOrderByResource(String action);

    /**
     * Find permissions by multiple resources
     */
    List<Permission> findByResourceIn(Set<String> resources);

    /**
     * Find permissions by multiple actions
     */
    List<Permission> findByActionIn(Set<String> actions);

    /**
     * Find permissions by names
     */
    List<Permission> findByNameIn(Set<String> names);

    /**
     * Find permissions assigned to roles
     */
    @Query("SELECT DISTINCT p FROM Permission p WHERE SIZE(p.roles) > 0")
    List<Permission> findPermissionsWithRoles();

    /**
     * Find permissions not assigned to any role
     */
    @Query("SELECT p FROM Permission p WHERE SIZE(p.roles) = 0")
    List<Permission> findPermissionsWithoutRoles();

    /**
     * Find permissions assigned to specific role
     */
    @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r.name = :roleName")
    List<Permission> findByRoleName(@Param("roleName") String roleName);

    /**
     * Find permissions assigned to specific role by role ID
     */
    @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r.id = :roleId")
    List<Permission> findByRoleId(@Param("roleId") Long roleId);

    /**
     * Find permissions for specific user (through roles)
     */
    @Query("SELECT DISTINCT p FROM Permission p JOIN p.roles r JOIN r.users u WHERE u.username = :username")
    List<Permission> findByUsername(@Param("username") String username);

    /**
     * Find permissions for specific user by user ID
     */
    @Query("SELECT DISTINCT p FROM Permission p JOIN p.roles r JOIN r.users u WHERE u.id = :userId")
    List<Permission> findByUserId(@Param("userId") Long userId);

    /**
     * Search permissions by name, description, resource, or action
     */
    @Query("SELECT p FROM Permission p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.resource) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.action) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Permission> searchPermissions(@Param("searchTerm") String searchTerm);

    /**
     * Find permissions by description containing keyword
     */
    @Query("SELECT p FROM Permission p WHERE LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Permission> findByDescriptionContaining(@Param("keyword") String keyword);

    /**
     * Find permissions by resource containing keyword
     */
    @Query("SELECT p FROM Permission p WHERE LOWER(p.resource) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Permission> findByResourceContaining(@Param("keyword") String keyword);

    /**
     * Find all unique resources
     */
    @Query("SELECT DISTINCT p.resource FROM Permission p ORDER BY p.resource")
    List<String> findAllResources();

    /**
     * Find all unique actions
     */
    @Query("SELECT DISTINCT p.action FROM Permission p ORDER BY p.action")
    List<String> findAllActions();

    /**
     * Find actions for specific resource
     */
    @Query("SELECT DISTINCT p.action FROM Permission p WHERE p.resource = :resource ORDER BY p.action")
    List<String> findActionsByResource(@Param("resource") String resource);

    /**
     * Find resources for specific action
     */
    @Query("SELECT DISTINCT p.resource FROM Permission p WHERE p.action = :action ORDER BY p.resource")
    List<String> findResourcesByAction(@Param("action") String action);

    /**
     * Count permissions by resource
     */
    long countByResource(String resource);

    /**
     * Count permissions by action
     */
    long countByAction(String action);

    /**
     * Count active permissions
     */
    long countByActiveTrue();

    /**
     * Count inactive permissions
     */
    long countByActiveFalse();

    /**
     * Count permissions with roles
     */
    @Query("SELECT COUNT(DISTINCT p) FROM Permission p WHERE SIZE(p.roles) > 0")
    long countPermissionsWithRoles();

    /**
     * Count permissions without roles
     */
    @Query("SELECT COUNT(p) FROM Permission p WHERE SIZE(p.roles) = 0")
    long countPermissionsWithoutRoles();

    /**
     * Count permissions by role
     */
    @Query("SELECT COUNT(p) FROM Permission p JOIN p.roles r WHERE r.name = :roleName")
    long countByRoleName(@Param("roleName") String roleName);

    /**
     * Get permission usage statistics
     */
    @Query("SELECT p.resource, p.action, p.name, SIZE(p.roles) as roleCount " +
           "FROM Permission p ORDER BY SIZE(p.roles) DESC, p.resource, p.action")
    List<Object[]> getPermissionUsageStatistics();

    /**
     * Get permission statistics by resource
     */
    @Query("SELECT p.resource, COUNT(p) as permissionCount, " +
           "SUM(CASE WHEN p.active = true THEN 1 ELSE 0 END) as activeCount, " +
           "AVG(SIZE(p.roles)) as avgRoleCount " +
           "FROM Permission p GROUP BY p.resource ORDER BY p.resource")
    List<Object[]> getPermissionStatsByResource();

    /**
     * Get permission statistics by action
     */
    @Query("SELECT p.action, COUNT(p) as permissionCount, " +
           "SUM(CASE WHEN p.active = true THEN 1 ELSE 0 END) as activeCount, " +
           "AVG(SIZE(p.roles)) as avgRoleCount " +
           "FROM Permission p GROUP BY p.action ORDER BY p.action")
    List<Object[]> getPermissionStatsByAction();

    /**
     * Find permissions that can be safely deleted (no roles assigned)
     */
    @Query("SELECT p FROM Permission p WHERE p.active = false AND SIZE(p.roles) = 0")
    List<Permission> findDeletablePermissions();

    /**
     * Find system permissions (typically non-deletable)
     */
    @Query("SELECT p FROM Permission p WHERE p.resource IN ('SYSTEM', 'ADMIN', 'SECURITY')")
    List<Permission> findSystemPermissions();

    /**
     * Find user-defined permissions
     */
    @Query("SELECT p FROM Permission p WHERE p.resource NOT IN ('SYSTEM', 'ADMIN', 'SECURITY')")
    List<Permission> findUserDefinedPermissions();

    /**
     * Find permissions created by specific user
     */
    @Query("SELECT p FROM Permission p WHERE p.createdBy = :username")
    List<Permission> findCreatedBy(@Param("username") String username);

    /**
     * Find most recently created permissions
     */
    @Query("SELECT p FROM Permission p ORDER BY p.createdAt DESC")
    List<Permission> findMostRecentlyCreated();

    /**
     * Check if user has specific permission (through roles)
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Permission p JOIN p.roles r JOIN r.users u WHERE u.username = :username AND p.name = :permissionName")
    boolean userHasPermission(@Param("username") String username, @Param("permissionName") String permissionName);

    /**
     * Check if user has permission for resource and action
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Permission p JOIN p.roles r JOIN r.users u WHERE u.username = :username AND p.resource = :resource AND p.action = :action")
    boolean userHasPermissionForResourceAndAction(@Param("username") String username, @Param("resource") String resource, @Param("action") String action);

    /**
     * Find permissions with duplicate resource and action combinations
     */
    @Query("SELECT p1 FROM Permission p1 WHERE EXISTS " +
           "(SELECT p2 FROM Permission p2 WHERE p2.id != p1.id AND p2.resource = p1.resource AND p2.action = p1.action)")
    List<Permission> findDuplicatePermissions();
}