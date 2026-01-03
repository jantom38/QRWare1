package com.qrware.repository.user;

import com.qrware.domain.user.Role;
import com.qrware.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepository extends BaseRepository<Role> {

    Optional<Role> findByName(String name);

    Optional<Role> findByNameIgnoreCase(String name);

    boolean existsByName(String name);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Role r WHERE r.name = :name AND r.id != :roleId")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("roleId") Long roleId);

    List<Role> findByActiveTrue();

    List<Role> findByActiveFalse();

    List<Role> findByActiveOrderByName(Boolean active);

    List<Role> findByNameIn(Set<String> names);

    @Query("SELECT DISTINCT r FROM Role r JOIN r.permissions p WHERE p.name = :permissionName")
    List<Role> findByPermissionName(@Param("permissionName") String permissionName);

    @Query("SELECT DISTINCT r FROM Role r JOIN r.permissions p WHERE p.name IN :permissionNames")
    List<Role> findByPermissionNames(@Param("permissionNames") List<String> permissionNames);

    @Query("SELECT DISTINCT r FROM Role r JOIN r.permissions p WHERE p.resource = :resource AND p.action = :action")
    List<Role> findByPermissionResourceAndAction(@Param("resource") String resource, @Param("action") String action);

    @Query("SELECT DISTINCT r FROM Role r JOIN r.permissions p WHERE p.resource = :resource")
    List<Role> findByPermissionResource(@Param("resource") String resource);

    @Query("SELECT DISTINCT r FROM Role r WHERE SIZE(r.users) > 0")
    List<Role> findRolesWithUsers();

    @Query("SELECT r FROM Role r WHERE SIZE(r.users) = 0")
    List<Role> findRolesWithoutUsers();

    @Query("SELECT r FROM Role r JOIN r.users u WHERE u.username = :username")
    List<Role> findByUsername(@Param("username") String username);

    @Query("SELECT r FROM Role r JOIN r.users u WHERE u.id = :userId")
    List<Role> findByUserId(@Param("userId") Long userId);

    @Query("SELECT r FROM Role r WHERE " +
           "LOWER(r.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Role> searchRoles(@Param("searchTerm") String searchTerm);

    @Query("SELECT r FROM Role r WHERE SIZE(r.permissions) >= :minPermissions")
    List<Role> findRolesWithMinimumPermissions(@Param("minPermissions") int minPermissions);

    @Query("SELECT r FROM Role r WHERE SIZE(r.permissions) <= :maxPermissions")
    List<Role> findRolesWithMaximumPermissions(@Param("maxPermissions") int maxPermissions);

    long countByActiveTrue();

    long countByActiveFalse();

    @Query("SELECT COUNT(DISTINCT r) FROM Role r WHERE SIZE(r.users) > 0")
    long countRolesWithUsers();

    @Query("SELECT COUNT(r) FROM Role r WHERE SIZE(r.users) = 0")
    long countRolesWithoutUsers();

    @Query("SELECT COUNT(DISTINCT r) FROM Role r JOIN r.permissions p WHERE p.name = :permissionName")
    long countByPermissionName(@Param("permissionName") String permissionName);

    @Query("SELECT r.name, r.description, SIZE(r.users) as userCount, SIZE(r.permissions) as permissionCount " +
           "FROM Role r ORDER BY SIZE(r.users) DESC")
    List<Object[]> getRoleUsageStatistics();

    @Query("SELECT r FROM Role r WHERE r.active = false AND SIZE(r.users) = 0")
    List<Role> findDeletableRoles();

    @Query("SELECT r FROM Role r WHERE r.name IN ('ADMIN', 'SYSTEM', 'SUPER_ADMIN')")
    List<Role> findSystemRoles();

    @Query("SELECT r FROM Role r WHERE r.name NOT IN ('ADMIN', 'SYSTEM', 'SUPER_ADMIN')")
    List<Role> findUserDefinedRoles();

    @Query("SELECT r FROM Role r WHERE r.createdBy = :username")
    List<Role> findCreatedBy(@Param("username") String username);

    @Query("SELECT r FROM Role r ORDER BY r.createdAt DESC")
    List<Role> findMostRecentlyCreated();

    @Query("SELECT r FROM Role r WHERE LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Role> findByDescriptionContaining(@Param("keyword") String keyword);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Role r JOIN r.permissions p WHERE r.id = :roleId AND p.name = :permissionName")
    boolean hasPermission(@Param("roleId") Long roleId, @Param("permissionName") String permissionName);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Role r JOIN r.permissions p WHERE r.id = :roleId AND p.resource = :resource AND p.action = :action")
    boolean hasPermissionForResourceAndAction(@Param("roleId") Long roleId, @Param("resource") String resource, @Param("action") String action);

    @Query("SELECT DISTINCT r1 FROM Role r1 JOIN r1.permissions p1 WHERE EXISTS " +
           "(SELECT r2 FROM Role r2 JOIN r2.permissions p2 WHERE r2.id != r1.id AND p2.id = p1.id)")
    List<Role> findRolesWithOverlappingPermissions();
}