package com.qrware.repository.user;

import com.qrware.domain.user.Permission;
import com.qrware.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PermissionRepository extends BaseRepository<Permission> {

    Optional<Permission> findByName(String name);

    Optional<Permission> findByNameIgnoreCase(String name);

    boolean existsByName(String name);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Permission p WHERE p.name = :name AND p.id != :permissionId")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("permissionId") Long permissionId);

    Optional<Permission> findByResourceAndAction(String resource, String action);

    boolean existsByResourceAndAction(String resource, String action);

    List<Permission> findByActiveTrue();

    List<Permission> findByActiveFalse();

    List<Permission> findByActiveOrderByName(Boolean active);

    List<Permission> findByResource(String resource);

    List<Permission> findByAction(String action);

    List<Permission> findByResourceOrderByAction(String resource);

    List<Permission> findByActionOrderByResource(String action);

    List<Permission> findByResourceIn(Set<String> resources);

    List<Permission> findByActionIn(Set<String> actions);

    List<Permission> findByNameIn(Set<String> names);

    @Query("SELECT DISTINCT p FROM Permission p WHERE SIZE(p.roles) > 0")
    List<Permission> findPermissionsWithRoles();

    @Query("SELECT p FROM Permission p WHERE SIZE(p.roles) = 0")
    List<Permission> findPermissionsWithoutRoles();

    @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r.name = :roleName")
    List<Permission> findByRoleName(@Param("roleName") String roleName);

    @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r.id = :roleId")
    List<Permission> findByRoleId(@Param("roleId") Long roleId);

    @Query("SELECT DISTINCT p FROM Permission p JOIN p.roles r JOIN r.users u WHERE u.username = :username")
    List<Permission> findByUsername(@Param("username") String username);

    @Query("SELECT DISTINCT p FROM Permission p JOIN p.roles r JOIN r.users u WHERE u.id = :userId")
    List<Permission> findByUserId(@Param("userId") Long userId);

    @Query("SELECT p FROM Permission p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.resource) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.action) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Permission> searchPermissions(@Param("searchTerm") String searchTerm);

    @Query("SELECT p FROM Permission p WHERE LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Permission> findByDescriptionContaining(@Param("keyword") String keyword);

    @Query("SELECT p FROM Permission p WHERE LOWER(p.resource) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Permission> findByResourceContaining(@Param("keyword") String keyword);

    @Query("SELECT DISTINCT p.resource FROM Permission p ORDER BY p.resource")
    List<String> findAllResources();

    @Query("SELECT DISTINCT p.action FROM Permission p ORDER BY p.action")
    List<String> findAllActions();

    @Query("SELECT DISTINCT p.action FROM Permission p WHERE p.resource = :resource ORDER BY p.action")
    List<String> findActionsByResource(@Param("resource") String resource);

    @Query("SELECT DISTINCT p.resource FROM Permission p WHERE p.action = :action ORDER BY p.resource")
    List<String> findResourcesByAction(@Param("action") String action);

    long countByResource(String resource);

    long countByAction(String action);

    long countByActiveTrue();

    long countByActiveFalse();

    @Query("SELECT COUNT(DISTINCT p) FROM Permission p WHERE SIZE(p.roles) > 0")
    long countPermissionsWithRoles();

    @Query("SELECT COUNT(p) FROM Permission p WHERE SIZE(p.roles) = 0")
    long countPermissionsWithoutRoles();

    @Query("SELECT COUNT(p) FROM Permission p JOIN p.roles r WHERE r.name = :roleName")
    long countByRoleName(@Param("roleName") String roleName);

    @Query("SELECT p.resource, p.action, p.name, SIZE(p.roles) as roleCount " +
           "FROM Permission p ORDER BY SIZE(p.roles) DESC, p.resource, p.action")
    List<Object[]> getPermissionUsageStatistics();

    @Query("SELECT p.resource, COUNT(p) as permissionCount, " +
           "SUM(CASE WHEN p.active = true THEN 1 ELSE 0 END) as activeCount, " +
           "AVG(SIZE(p.roles)) as avgRoleCount " +
           "FROM Permission p GROUP BY p.resource ORDER BY p.resource")
    List<Object[]> getPermissionStatsByResource();

    @Query("SELECT p.action, COUNT(p) as permissionCount, " +
           "SUM(CASE WHEN p.active = true THEN 1 ELSE 0 END) as activeCount, " +
           "AVG(SIZE(p.roles)) as avgRoleCount " +
           "FROM Permission p GROUP BY p.action ORDER BY p.action")
    List<Object[]> getPermissionStatsByAction();

    @Query("SELECT p FROM Permission p WHERE p.active = false AND SIZE(p.roles) = 0")
    List<Permission> findDeletablePermissions();

    @Query("SELECT p FROM Permission p WHERE p.resource IN ('SYSTEM', 'ADMIN', 'SECURITY')")
    List<Permission> findSystemPermissions();

    @Query("SELECT p FROM Permission p WHERE p.resource NOT IN ('SYSTEM', 'ADMIN', 'SECURITY')")
    List<Permission> findUserDefinedPermissions();

    @Query("SELECT p FROM Permission p WHERE p.createdBy = :username")
    List<Permission> findCreatedBy(@Param("username") String username);

    @Query("SELECT p FROM Permission p ORDER BY p.createdAt DESC")
    List<Permission> findMostRecentlyCreated();

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Permission p JOIN p.roles r JOIN r.users u WHERE u.username = :username AND p.name = :permissionName")
    boolean userHasPermission(@Param("username") String username, @Param("permissionName") String permissionName);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Permission p JOIN p.roles r JOIN r.users u WHERE u.username = :username AND p.resource = :resource AND p.action = :action")
    boolean userHasPermissionForResourceAndAction(@Param("username") String username, @Param("resource") String resource, @Param("action") String action);

    @Query("SELECT p1 FROM Permission p1 WHERE EXISTS " +
           "(SELECT p2 FROM Permission p2 WHERE p2.id != p1.id AND p2.resource = p1.resource AND p2.action = p1.action)")
    List<Permission> findDuplicatePermissions();
}