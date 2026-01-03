package com.qrware.repository.user;

import com.qrware.domain.user.User;
import com.qrware.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends BaseRepository<User> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
    Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.username = :username AND u.id != :userId")
    boolean existsByUsernameAndIdNot(@Param("username") String username, @Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email AND u.id != :userId")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("userId") Long userId);

    List<User> findByActiveTrue();

    List<User> findByActiveFalse();

    List<User> findByEmailVerified(Boolean emailVerified);

    @Query("SELECT u FROM User u WHERE u.failedLoginAttempts >= :threshold")
    List<User> findByFailedLoginAttemptsGreaterThanEqual(@Param("threshold") Integer threshold);

    @Query("SELECT u FROM User u WHERE u.lockedUntil IS NOT NULL AND u.lockedUntil > CURRENT_TIMESTAMP")
    List<User> findLockedUsers();

    @Query("SELECT u FROM User u WHERE u.lockedUntil IS NOT NULL AND u.lockedUntil <= :date")
    List<User> findUsersLockedUntil(@Param("date") LocalDateTime date);

    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);

    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name IN :roleNames")
    List<User> findByRoleNames(@Param("roleNames") List<String> roleNames);

    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r JOIN r.permissions p WHERE p.name = :permissionName")
    List<User> findByPermissionName(@Param("permissionName") String permissionName);

    @Query("SELECT u FROM User u WHERE u.lastLogin IS NULL OR u.lastLogin < :date")
    List<User> findUsersNotLoggedInSince(@Param("date") LocalDateTime date);

    @Query("SELECT u FROM User u WHERE u.lastLogin >= :date")
    List<User> findUsersLoggedInAfter(@Param("date") LocalDateTime date);

    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')) AND LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))")
    List<User> findByFirstNameAndLastNameContainingIgnoreCase(@Param("firstName") String firstName, @Param("lastName") String lastName);

    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<User> findByNameContainingIgnoreCase(@Param("name") String name);

    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchUsers(@Param("searchTerm") String searchTerm);

    Optional<User> findByPhone(String phone);

    long countByActiveTrue();

    long countByActiveFalse();

    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
    long countByRoleName(@Param("roleName") String roleName);

    @Query("SELECT COUNT(u) FROM User u WHERE u.lockedUntil IS NOT NULL AND u.lockedUntil > CURRENT_TIMESTAMP")
    long countLockedUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.failedLoginAttempts > 0")
    long countUsersWithFailedAttempts();

    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = 0 WHERE u.id = :userId")
    void resetFailedLoginAttempts(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :loginTime WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime);

    @Modifying
    @Query("UPDATE User u SET u.lockedUntil = NULL, u.failedLoginAttempts = 0 WHERE u.id = :userId")
    void unlockUser(@Param("userId") Long userId);

    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findByRoleNameAndCreatedBetween(@Param("roleName") String roleName, 
                                              @Param("startDate") LocalDateTime startDate, 
                                              @Param("endDate") LocalDateTime endDate);

    @Query("SELECT u FROM User u WHERE u.emailVerified = false AND u.createdAt < :expirationDate")
    List<User> findUsersWithExpiredEmailVerification(@Param("expirationDate") LocalDateTime expirationDate);

    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<User> searchUsers(@Param("query") String query, Pageable pageable);

    @Query("SELECT " +
           "COUNT(u) as totalUsers, " +
           "SUM(CASE WHEN u.active = true THEN 1 ELSE 0 END) as activeUsers, " +
           "SUM(CASE WHEN u.emailVerified = true THEN 1 ELSE 0 END) as verifiedUsers, " +
           "SUM(CASE WHEN u.lockedUntil IS NOT NULL AND u.lockedUntil > CURRENT_TIMESTAMP THEN 1 ELSE 0 END) as lockedUsers " +
           "FROM User u")
    Object[] getUserStatistics();
}