package com.qrware.repository.product;

import com.qrware.domain.product.Category;
import com.qrware.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Category entity operations
 */
@Repository
public interface CategoryRepository extends BaseRepository<Category> {

    /**
     * Find category by name
     */
    Optional<Category> findByName(String name);

    /**
     * Find category by name ignoring case
     */
    Optional<Category> findByNameIgnoreCase(String name);

    /**
     * Find category by code
     */
    Optional<Category> findByCode(String code);

    /**
     * Find category by code ignoring case
     */
    Optional<Category> findByCodeIgnoreCase(String code);

    /**
     * Check if name exists
     */
    boolean existsByName(String name);

    /**
     * Check if name exists excluding current category
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Category c WHERE c.name = :name AND c.id != :categoryId")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("categoryId") Long categoryId);

    /**
     * Check if code exists
     */
    boolean existsByCode(String code);

    /**
     * Check if code exists excluding current category
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Category c WHERE c.code = :code AND c.id != :categoryId")
    boolean existsByCodeAndIdNot(@Param("code") String code, @Param("categoryId") Long categoryId);

    /**
     * Find all active categories
     */
    List<Category> findByActiveTrue();

    /**
     * Find all inactive categories
     */
    List<Category> findByActiveFalse();

    /**
     * Find categories by active status ordered by sort order and name
     */
    List<Category> findByActiveOrderBySortOrderAscNameAsc(Boolean active);

    /**
     * Find all root categories (no parent)
     */
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL ORDER BY c.sortOrder ASC, c.name ASC")
    List<Category> findRootCategories();

    /**
     * Find active root categories
     */
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.active = true ORDER BY c.sortOrder ASC, c.name ASC")
    List<Category> findActiveRootCategories();

    /**
     * Find children of specific category
     */
    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId ORDER BY c.sortOrder ASC, c.name ASC")
    List<Category> findByParentId(@Param("parentId") Long parentId);

    /**
     * Find active children of specific category
     */
    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId AND c.active = true ORDER BY c.sortOrder ASC, c.name ASC")
    List<Category> findActiveByParentId(@Param("parentId") Long parentId);

    /**
     * Find categories by parent code
     */
    @Query("SELECT c FROM Category c WHERE c.parent.code = :parentCode ORDER BY c.sortOrder ASC, c.name ASC")
    List<Category> findByParentCode(@Param("parentCode") String parentCode);

    /**
     * Find categories by parent name
     */
    @Query("SELECT c FROM Category c WHERE c.parent.name = :parentName ORDER BY c.sortOrder ASC, c.name ASC")
    List<Category> findByParentName(@Param("parentName") String parentName);

    /**
     * Find all descendant categories (recursive)
     */
    @Query(value = "WITH RECURSIVE category_tree AS (" +
           "SELECT id, name, code, parent_id, 0 as level FROM categories WHERE id = :categoryId " +
           "UNION ALL " +
           "SELECT c.id, c.name, c.code, c.parent_id, ct.level + 1 " +
           "FROM categories c JOIN category_tree ct ON c.parent_id = ct.id" +
           ") SELECT * FROM category_tree WHERE level > 0", nativeQuery = true)
    List<Object[]> findAllDescendants(@Param("categoryId") Long categoryId);

    /**
     * Find all ancestor categories (recursive)
     */
    @Query(value = "WITH RECURSIVE category_path AS (" +
           "SELECT id, name, code, parent_id, 0 as level FROM categories WHERE id = :categoryId " +
           "UNION ALL " +
           "SELECT c.id, c.name, c.code, c.parent_id, cp.level - 1 " +
           "FROM categories c JOIN category_path cp ON c.id = cp.parent_id" +
           ") SELECT * FROM category_path WHERE level < 0 ORDER BY level", nativeQuery = true)
    List<Object[]> findAllAncestors(@Param("categoryId") Long categoryId);

    /**
     * Find categories by name containing keyword
     */
    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Category> findByNameContainingIgnoreCase(@Param("keyword") String keyword);

    /**
     * Find categories by description containing keyword
     */
    @Query("SELECT c FROM Category c WHERE LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Category> findByDescriptionContainingIgnoreCase(@Param("keyword") String keyword);

    /**
     * Search categories by name, code, or description
     */
    @Query("SELECT c FROM Category c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Category> searchCategories(@Param("searchTerm") String searchTerm);

    /**
     * Find categories requiring special handling
     */
    List<Category> findByRequiresSpecialHandlingTrue();

    /**
     * Find categories not requiring special handling
     */
    List<Category> findByRequiresSpecialHandlingFalse();

    /**
     * Find categories with storage temperature requirements
     */
    @Query("SELECT c FROM Category c WHERE c.storageTemperatureMin IS NOT NULL OR c.storageTemperatureMax IS NOT NULL")
    List<Category> findCategoriesWithTemperatureRequirements();

    /**
     * Find categories with storage humidity requirements
     */
    @Query("SELECT c FROM Category c WHERE c.storageHumidityMin IS NOT NULL OR c.storageHumidityMax IS NOT NULL")
    List<Category> findCategoriesWithHumidityRequirements();

    /**
     * Find categories with any storage requirements
     */
    @Query("SELECT c FROM Category c WHERE c.storageTemperatureMin IS NOT NULL OR c.storageTemperatureMax IS NOT NULL OR c.storageHumidityMin IS NOT NULL OR c.storageHumidityMax IS NOT NULL")
    List<Category> findCategoriesWithStorageRequirements();

    /**
     * Find categories by temperature range
     */
    @Query("SELECT c FROM Category c WHERE " +
           "(:temperature IS NULL OR c.storageTemperatureMin IS NULL OR :temperature >= c.storageTemperatureMin) AND " +
           "(:temperature IS NULL OR c.storageTemperatureMax IS NULL OR :temperature <= c.storageTemperatureMax)")
    List<Category> findCategoriesForTemperature(@Param("temperature") Integer temperature);

    /**
     * Find categories by humidity range
     */
    @Query("SELECT c FROM Category c WHERE " +
           "(:humidity IS NULL OR c.storageHumidityMin IS NULL OR :humidity >= c.storageHumidityMin) AND " +
           "(:humidity IS NULL OR c.storageHumidityMax IS NULL OR :humidity <= c.storageHumidityMax)")
    List<Category> findCategoriesForHumidity(@Param("humidity") Integer humidity);

    /**
     * Find categories with products
     */
    @Query("SELECT DISTINCT c FROM Category c WHERE SIZE(c.products) > 0")
    List<Category> findCategoriesWithProducts();

    /**
     * Find categories without products
     */
    @Query("SELECT c FROM Category c WHERE SIZE(c.products) = 0")
    List<Category> findCategoriesWithoutProducts();

    /**
     * Find categories with children
     */
    @Query("SELECT DISTINCT c FROM Category c WHERE SIZE(c.children) > 0")
    List<Category> findCategoriesWithChildren();

    /**
     * Find leaf categories (no children)
     */
    @Query("SELECT c FROM Category c WHERE SIZE(c.children) = 0")
    List<Category> findLeafCategories();

    /**
     * Find categories that can be deleted (no products and no children)
     */
    @Query("SELECT c FROM Category c WHERE SIZE(c.products) = 0 AND SIZE(c.children) = 0")
    List<Category> findDeletableCategories();

    /**
     * Find categories by level (depth in hierarchy)
     */
    @Query(value = "WITH RECURSIVE category_levels AS (" +
           "SELECT id, name, parent_id, 0 as level FROM categories WHERE parent_id IS NULL " +
           "UNION ALL " +
           "SELECT c.id, c.name, c.parent_id, cl.level + 1 " +
           "FROM categories c JOIN category_levels cl ON c.parent_id = cl.id" +
           ") SELECT id FROM category_levels WHERE level = :level", nativeQuery = true)
    List<Long> findCategoryIdsByLevel(@Param("level") Integer level);

    /**
     * Get maximum category level (depth)
     */
    @Query(value = "WITH RECURSIVE category_levels AS (" +
           "SELECT id, parent_id, 0 as level FROM categories WHERE parent_id IS NULL " +
           "UNION ALL " +
           "SELECT c.id, c.parent_id, cl.level + 1 " +
           "FROM categories c JOIN category_levels cl ON c.parent_id = cl.id" +
           ") SELECT MAX(level) FROM category_levels", nativeQuery = true)
    Integer findMaxCategoryLevel();

    /**
     * Count categories by parent
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE c.parent.id = :parentId")
    long countByParentId(@Param("parentId") Long parentId);

    /**
     * Count root categories
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE c.parent IS NULL")
    long countRootCategories();

    /**
     * Count active categories
     */
    long countByActiveTrue();

    /**
     * Count inactive categories
     */
    long countByActiveFalse();

    /**
     * Count categories with products
     */
    @Query("SELECT COUNT(DISTINCT c) FROM Category c WHERE SIZE(c.products) > 0")
    long countCategoriesWithProducts();

    /**
     * Count categories without products
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE SIZE(c.products) = 0")
    long countCategoriesWithoutProducts();

    /**
     * Get category statistics
     */
    @Query("SELECT " +
           "COUNT(c) as totalCategories, " +
           "SUM(CASE WHEN c.active = true THEN 1 ELSE 0 END) as activeCategories, " +
           "SUM(CASE WHEN c.parent IS NULL THEN 1 ELSE 0 END) as rootCategories, " +
           "SUM(CASE WHEN SIZE(c.children) = 0 THEN 1 ELSE 0 END) as leafCategories, " +
           "SUM(CASE WHEN SIZE(c.products) > 0 THEN 1 ELSE 0 END) as categoriesWithProducts " +
           "FROM Category c")
    Object[] getCategoryStatistics();

    /**
     * Get categories with product count
     */
    @Query("SELECT c, SIZE(c.products) as productCount FROM Category c ORDER BY SIZE(c.products) DESC")
    List<Object[]> getCategoriesWithProductCount();

    /**
     * Get categories with children count
     */
    @Query("SELECT c, SIZE(c.children) as childrenCount FROM Category c ORDER BY SIZE(c.children) DESC")
    List<Object[]> getCategoriesWithChildrenCount();

    /**
     * Find categories created by user
     */
    @Query("SELECT c FROM Category c WHERE c.createdBy = :username")
    List<Category> findCreatedBy(@Param("username") String username);

    /**
     * Find most recently created categories
     */
    @Query("SELECT c FROM Category c ORDER BY c.createdAt DESC")
    List<Category> findMostRecentlyCreated();

    /**
     * Find categories by sort order range
     */
    @Query("SELECT c FROM Category c WHERE c.sortOrder BETWEEN :minOrder AND :maxOrder ORDER BY c.sortOrder ASC")
    List<Category> findBySortOrderBetween(@Param("minOrder") Integer minOrder, @Param("maxOrder") Integer maxOrder);

    /**
     * Find next available sort order for parent
     */
    @Query("SELECT COALESCE(MAX(c.sortOrder), 0) + 1 FROM Category c WHERE " +
           "(:parentId IS NULL AND c.parent IS NULL) OR " +
           "(:parentId IS NOT NULL AND c.parent.id = :parentId)")
    Integer findNextSortOrder(@Param("parentId") Long parentId);

    /**
     * Find categories by color
     */
    List<Category> findByColor(String color);

    /**
     * Find categories by icon
     */
    List<Category> findByIcon(String icon);

    /**
     * Find all unique colors
     */
    @Query("SELECT DISTINCT c.color FROM Category c WHERE c.color IS NOT NULL ORDER BY c.color")
    List<String> findAllColors();

    /**
     * Find all unique icons
     */
    @Query("SELECT DISTINCT c.icon FROM Category c WHERE c.icon IS NOT NULL ORDER BY c.icon")
    List<String> findAllIcons();
}