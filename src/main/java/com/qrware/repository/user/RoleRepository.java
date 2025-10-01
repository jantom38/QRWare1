package com.qrware.repository.user;

import com.qrware.domain.user.Role;
import com.qrware.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository interface for Role entity operations
 */
@Repository
public interface RoleRepository extends BaseRepository<Role> {

    /**
     * Find role by name
     */
    Optional<Role> findByName(String name);

    /**
     * Find role by name ignoring case
     */
    Optional<Role> findByNameIgnoreCase(String name);

    /**
     * Check if role exists by name
     */
    boolean existsByName(String name);

    /**
     * Check if role name exists excluding current role
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Role r WHERE r.name = :name AND r.id != :roleId")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("roleId") Long roleId);

    /**
     * Find all active roles
     */
    List<Role> findByActiveTrue();

    /**
     * Find all inactive roles
     */
    List<Role> findByActiveFalse();

    /**
     * Find roles by active status ordered by name
     */
    List<Role> findByActiveOrderByName(Boolean active);

    /**
     * Find roles by names
     */
    List<Role> findByNameIn(Set<String> names);

    /**
     * Find roles containing specific permission
     */
    @Query("SELECT DISTINCT r FROM Role r JOIN r.permissions p WHERE p.name = :permissionName")
    List<Role> findByPermissionName(@Param("permissionName") String permissionName);

    /**
     * Find roles containing any of the specified permissions
     */
    @Query("SELECT DISTINCT r FROM Role r JOIN r.permissions p WHERE p.name IN :permissionNames")
    List<Role> findByPermissionNames(@Param("permissionNames") List<String> permissionNames);

    /**
     * Find roles by permission resource and action
     */
    @Query("SELECT DISTINCT r FROM Role r JOIN r.permissions p WHERE p.resource = :resource AND p.action = :action")
    List<Role> findByPermissionResourceAndAction(@Param("resource") String resource, @Param("action") String action);

    /**
     * Find roles by permission resource
     */
    @Query("SELECT DISTINCT r FROM Role r JOIN r.permissions p WHERE p.resource = :resource")
    List<Role> findByPermissionResource(@Param("resource") String resource);

    /**
     * Find roles assigned to users
     */
    @Query("SELECT DISTINCT r FROM Role r WHERE SIZE(r.users) > 0")
    List<Role> findRolesWithUsers();

    /**
     * Find roles not assigned to any user
     */
    @Query("SELECT r FROM Role r WHERE SIZE(r.users) = 0")
    List<Role> findRolesWithoutUsers();

    /**
     * Find roles assigned to specific user
     */
    @Query("SELECT r FROM Role r JOIN r.users u WHERE u.username = :username")
    List<Role> findByUsername(@Param("username") String username);

    /**
     * Find roles assigned to specific user by user ID
     */
    @Query("SELECT r FROM Role r JOIN r.users u WHERE u.id = :userId")
    List<Role> findByUserId(@Param("userId") Long userId);

    /**
     * Search roles by name or description
     */
    @Query("SELECT r FROM Role r WHERE " +
           "LOWER(r.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Role> searchRoles(@Param("searchTerm") String searchTerm);

    /**
     * Find roles with minimum number of permissions
     */
    @Query("SELECT r FROM Role r WHERE SIZE(r.permissions) >= :minPermissions")
    List<Role> findRolesWithMinimumPermissions(@Param("minPermissions") int minPermissions);

    /**
     * Find roles with maximum number of permissions
     */
    @Query("SELECT r FROM Role r WHERE SIZE(r.permissions) <= :maxPermissions")
    List<Role> findRolesWithMaximumPermissions(@Param("maxPermissions") int maxPermissions);

    /**
     * Count active roles
     */
    long countByActiveTrue();

    /**
     * Count inactive roles
     */
    long countByActiveFalse();

    /**
     * Count roles with users
     */
    @Query("SELECT COUNT(DISTINCT r) FROM Role r WHERE SIZE(r.users) > 0")
    long countRolesWithUsers();

    /**
     * Count roles without users
     */
    @Query("SELECT COUNT(r) FROM Role r WHERE SIZE(r.users) = 0")
    long countRolesWithoutUsers();

    /**
     * Count roles by permission
     */
    @Query("SELECT COUNT(DISTINCT r) FROM Role r JOIN r.permissions p WHERE p.name = :permissionName")
    long countByPermissionName(@Param("permissionName") String permissionName);

    /**
     * Get role usage statistics
     */
    @Query("SELECT r.name, r.description, SIZE(r.users) as userCount, SIZE(r.permissions) as permissionCount " +
           "FROM Role r ORDER BY SIZE(r.users) DESC")
    List<Object[]> getRoleUsageStatistics();

    /**
     * Find roles that can be safely deleted (no users assigned)
     */
    @Query("SELECT r FROM Role r WHERE r.active = false AND SIZE(r.users) = 0")
    List<Role> findDeletableRoles();

    /**
     * Find system roles (typically non-deletable)
     */
    @Query("SELECT r FROM Role r WHERE r.name IN ('ADMIN', 'SYSTEM', 'SUPER_ADMIN')")
    List<Role> findSystemRoles();

    /**
     * Find user-defined roles
     */
    @Query("SELECT r FROM Role r WHERE r.name NOT IN ('ADMIN', 'SYSTEM', 'SUPER_ADMIN')")
    List<Role> findUserDefinedRoles();

    /**
     * Find roles created by specific user
     */
    @Query("SELECT r FROM Role r WHERE r.createdBy = :username")
    List<Role> findCreatedBy(@Param("username") String username);

    /**
     * Find most recently created roles
     */
    @Query("SELECT r FROM Role r ORDER BY r.createdAt DESC")
    List<Role> findMostRecentlyCreated();

    /**
     * Find roles by description containing keyword
     */
    @Query("SELECT r FROM Role r WHERE LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Role> findByDescriptionContaining(@Param("keyword") String keyword);

    /**
     * Check if role has specific permission
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Role r JOIN r.permissions p WHERE r.id = :roleId AND p.name = :permissionName")
    boolean hasPermission(@Param("roleId") Long roleId, @Param("permissionName") String permissionName);

    /**
     * Check if role has permission for resource and action
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Role r JOIN r.permissions p WHERE r.id = :roleId AND p.resource = :resource AND p.action = :action")
    boolean hasPermissionForResourceAndAction(@Param("roleId") Long roleId, @Param("resource") String resource, @Param("action") String action);

    /**
     * Find roles with overlapping permissions (roles that share at least one permission)
     */
    @Query("SELECT DISTINCT r1 FROM Role r1 JOIN r1.permissions p1 WHERE EXISTS " +
           "(SELECT r2 FROM Role r2 JOIN r2.permissions p2 WHERE r2.id != r1.id AND p2.id = p1.id)")
    List<Role> findRolesWithOverlappingPermissions();
}