package com.qrware.repository;

import com.qrware.domain.common.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Base repository interface providing common operations for all entities
 * 
 * @param <T> Entity type extending BaseEntity
 */
@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity> extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {

    /**
     * Find entity by ID if it exists and is not soft deleted
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id")
    Optional<T> findByIdIfExists(@Param("id") Long id);

    /**
     * Find all entities created after specified date
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.createdAt >= :date ORDER BY e.createdAt DESC")
    List<T> findAllCreatedAfter(@Param("date") LocalDateTime date);

    /**
     * Find all entities created between dates
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.createdAt BETWEEN :startDate AND :endDate ORDER BY e.createdAt DESC")
    List<T> findAllCreatedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find all entities updated after specified date
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.updatedAt >= :date ORDER BY e.updatedAt DESC")
    List<T> findAllUpdatedAfter(@Param("date") LocalDateTime date);

    /**
     * Find all entities created by user
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.createdBy = :username ORDER BY e.createdAt DESC")
    List<T> findAllCreatedBy(@Param("username") String username);

    /**
     * Find all entities updated by user
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.updatedBy = :username ORDER BY e.updatedAt DESC")
    List<T> findAllUpdatedBy(@Param("username") String username);

    /**
     * Count entities created after specified date
     */
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.createdAt >= :date")
    long countCreatedAfter(@Param("date") LocalDateTime date);

    /**
     * Count entities created between dates
     */
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.createdAt BETWEEN :startDate AND :endDate")
    long countCreatedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find entities with specific version (for optimistic locking)
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.version = :version")
    List<T> findByVersion(@Param("version") Long version);

    /**
     * Bulk update created by field
     */
    @Modifying
    @Query("UPDATE #{#entityName} e SET e.createdBy = :newCreatedBy WHERE e.createdBy = :oldCreatedBy")
    int updateCreatedBy(@Param("oldCreatedBy") String oldCreatedBy, @Param("newCreatedBy") String newCreatedBy);

    /**
     * Get the latest entity by creation date
     */
    @Query("SELECT e FROM #{#entityName} e ORDER BY e.createdAt DESC LIMIT 1")
    Optional<T> findLatest();

    /**
     * Get the oldest entity by creation date
     */
    @Query("SELECT e FROM #{#entityName} e ORDER BY e.createdAt ASC LIMIT 1")
    Optional<T> findOldest();

    /**
     * Check if entity exists by ID
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM #{#entityName} e WHERE e.id = :id")
    boolean existsByIdCustom(@Param("id") Long id);

    /**
     * Get total count of entities
     */
    @Query("SELECT COUNT(e) FROM #{#entityName} e")
    long getTotalCount();
}