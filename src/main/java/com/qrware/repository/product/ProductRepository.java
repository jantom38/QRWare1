package com.qrware.repository.product;

import com.qrware.domain.product.Product;
import com.qrware.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Product entity operations
 */
@Repository
public interface ProductRepository extends BaseRepository<Product> {

    /**
     * Find product by SKU
     */
    Optional<Product> findBySku(String sku);

    /**
     * Find product by SKU ignoring case
     */
    Optional<Product> findBySkuIgnoreCase(String sku);

    /**
     * Find product by barcode
     */
    Optional<Product> findByBarcode(String barcode);

    /**
     * Check if SKU exists
     */
    boolean existsBySku(String sku);

    /**
     * Check if SKU exists excluding current product
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Product p WHERE p.sku = :sku AND p.id != :productId")
    boolean existsBySkuAndIdNot(@Param("sku") String sku, @Param("productId") Long productId);

    /**
     * Check if barcode exists
     */
    boolean existsByBarcode(String barcode);

    /**
     * Check if barcode exists excluding current product
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Product p WHERE p.barcode = :barcode AND p.id != :productId")
    boolean existsByBarcodeAndIdNot(@Param("barcode") String barcode, @Param("productId") Long productId);

    /**
     * Find all active products
     */
    List<Product> findByActiveTrue();

    /**
     * Find all inactive products
     */
    List<Product> findByActiveFalse();

    Page<Product> findByActive(Boolean active, Pageable pageable);

    /**
     * Find products by category ID
     */
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId")
    List<Product> findByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * Find products by category name
     */
    @Query("SELECT p FROM Product p WHERE p.category.name = :categoryName")
    List<Product> findByCategoryName(@Param("categoryName") String categoryName);

    /**
     * Find products by category code
     */
    @Query("SELECT p FROM Product p WHERE p.category.code = :categoryCode")
    List<Product> findByCategoryCode(@Param("categoryCode") String categoryCode);

    /**
     * Find products by name containing keyword
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> findByNameContainingIgnoreCase(@Param("keyword") String keyword);

    /**
     * Find products by description containing keyword
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> findByDescriptionContainingIgnoreCase(@Param("keyword") String keyword);

    /**
     * Search products by SKU, name, or description
     */
    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Product> searchProducts(@Param("searchTerm") String searchTerm);

    /**
     * Find products by manufacturer
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.manufacturer) LIKE LOWER(CONCAT('%', :manufacturer, '%'))")
    List<Product> findByManufacturerContainingIgnoreCase(@Param("manufacturer") String manufacturer);

    /**
     * Find products by supplier
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.supplier) LIKE LOWER(CONCAT('%', :supplier, '%'))")
    List<Product> findBySupplierContainingIgnoreCase(@Param("supplier") String supplier);

    /**
     * Find products by unit of measure
     */
    List<Product> findByUnitOfMeasure(String unitOfMeasure);

    /**
     * Find products with price range
     */
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    List<Product> findByPriceBetween(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    /**
     * Find products with cost range
     */
    @Query("SELECT p FROM Product p WHERE p.cost BETWEEN :minCost AND :maxCost")
    List<Product> findByCostBetween(@Param("minCost") BigDecimal minCost, @Param("maxCost") BigDecimal maxCost);

    /**
     * Find products with weight range
     */
    @Query("SELECT p FROM Product p WHERE p.weight BETWEEN :minWeight AND :maxWeight")
    List<Product> findByWeightBetween(@Param("minWeight") BigDecimal minWeight, @Param("maxWeight") BigDecimal maxWeight);

    /**
     * Find perishable products
     */
    List<Product> findByPerishableTrue();

    /**
     * Find non-perishable products
     */
    List<Product> findByPerishableFalse();

    /**
     * Find hazardous products
     */
    List<Product> findByHazardousTrue();

    /**
     * Find non-hazardous products
     */
    List<Product> findByHazardousFalse();

    /**
     * Find fragile products
     */
    List<Product> findByFragileTrue();

    /**
     * Find non-fragile products
     */
    List<Product> findByFragileFalse();

    /**
     * Find products requiring special handling
     */
    @Query("SELECT p FROM Product p WHERE p.perishable = true OR p.hazardous = true OR p.fragile = true")
    List<Product> findProductsRequiringSpecialHandling();

    /**
     * Find products with low minimum stock
     */
    @Query("SELECT p FROM Product p WHERE p.minimumStock <= :threshold")
    List<Product> findByMinimumStockLessThanEqual(@Param("threshold") Integer threshold);

    /**
     * Find products with high minimum stock
     */
    @Query("SELECT p FROM Product p WHERE p.minimumStock >= :threshold")
    List<Product> findByMinimumStockGreaterThanEqual(@Param("threshold") Integer threshold);

    /**
     * Find products with reorder point set
     */
    @Query("SELECT p FROM Product p WHERE p.reorderPoint IS NOT NULL")
    List<Product> findProductsWithReorderPoint();

    /**
     * Find products without reorder point
     */
    @Query("SELECT p FROM Product p WHERE p.reorderPoint IS NULL")
    List<Product> findProductsWithoutReorderPoint();

    /**
     * Find products with maximum stock set
     */
    @Query("SELECT p FROM Product p WHERE p.maximumStock IS NOT NULL")
    List<Product> findProductsWithMaximumStock();

    /**
     * Find products without maximum stock
     */
    @Query("SELECT p FROM Product p WHERE p.maximumStock IS NULL")
    List<Product> findProductsWithoutMaximumStock();

    /**
     * Find products by storage conditions
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.storageConditions) LIKE LOWER(CONCAT('%', :condition, '%'))")
    List<Product> findByStorageConditionsContaining(@Param("condition") String condition);

    /**
     * Count products by category
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    long countByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * Count active products
     */
    long countByActiveTrue();

    /**
     * Count inactive products
     */
    long countByActiveFalse();

    /**
     * Count perishable products
     */
    long countByPerishableTrue();

    /**
     * Count hazardous products
     */
    long countByHazardousTrue();

    /**
     * Count fragile products
     */
    long countByFragileTrue();

    /**
     * Get product statistics
     */
    @Query("SELECT " +
           "COUNT(p) as totalProducts, " +
           "SUM(CASE WHEN p.active = true THEN 1 ELSE 0 END) as activeProducts, " +
           "SUM(CASE WHEN p.perishable = true THEN 1 ELSE 0 END) as perishableProducts, " +
           "SUM(CASE WHEN p.hazardous = true THEN 1 ELSE 0 END) as hazardousProducts, " +
           "SUM(CASE WHEN p.fragile = true THEN 1 ELSE 0 END) as fragileProducts " +
           "FROM Product p")
    Object[] getProductStatistics();

    /**
     * Get products with inventory count
     */
    @Query("SELECT p, SIZE(p.inventoryItems) as inventoryCount FROM Product p ORDER BY SIZE(p.inventoryItems) DESC")
    List<Object[]> getProductsWithInventoryCount();

    /**
     * Find products without inventory
     */
    @Query("SELECT p FROM Product p WHERE SIZE(p.inventoryItems) = 0")
    List<Product> findProductsWithoutInventory();

    /**
     * Find products with inventory
     */
    @Query("SELECT DISTINCT p FROM Product p WHERE SIZE(p.inventoryItems) > 0")
    List<Product> findProductsWithInventory();

    /**
     * Find products by price above average
     */
    @Query("SELECT p FROM Product p WHERE p.price > (SELECT AVG(p2.price) FROM Product p2 WHERE p2.price IS NOT NULL)")
    List<Product> findProductsWithPriceAboveAverage();

    /**
     * Find products by cost above average
     */
    @Query("SELECT p FROM Product p WHERE p.cost > (SELECT AVG(p2.cost) FROM Product p2 WHERE p2.cost IS NOT NULL)")
    List<Product> findProductsWithCostAboveAverage();

    /**
     * Find all unique manufacturers
     */
    @Query("SELECT DISTINCT p.manufacturer FROM Product p WHERE p.manufacturer IS NOT NULL ORDER BY p.manufacturer")
    List<String> findAllManufacturers();

    /**
     * Find all unique suppliers
     */
    @Query("SELECT DISTINCT p.supplier FROM Product p WHERE p.supplier IS NOT NULL ORDER BY p.supplier")
    List<String> findAllSuppliers();

    /**
     * Find all unique units of measure
     */
    @Query("SELECT DISTINCT p.unitOfMeasure FROM Product p ORDER BY p.unitOfMeasure")
    List<String> findAllUnitsOfMeasure();

    /**
     * Find products created by user
     */
    @Query("SELECT p FROM Product p WHERE p.createdBy = :username")
    List<Product> findCreatedBy(@Param("username") String username);

    /**
     * Find most recently created products
     */
    @Query("SELECT p FROM Product p ORDER BY p.createdAt DESC")
    List<Product> findMostRecentlyCreated();

    /**
     * Find most recently updated products
     */
    @Query("SELECT p FROM Product p ORDER BY p.updatedAt DESC")
    List<Product> findMostRecentlyUpdated();

    /**
     * Find products with highest margin
     */
    @Query("SELECT p FROM Product p WHERE p.price IS NOT NULL AND p.cost IS NOT NULL ORDER BY (p.price - p.cost) DESC")
    List<Product> findProductsWithHighestMargin();

    /**
     * Find products with lowest margin
     */
    @Query("SELECT p FROM Product p WHERE p.price IS NOT NULL AND p.cost IS NOT NULL ORDER BY (p.price - p.cost) ASC")
    List<Product> findProductsWithLowestMargin();

    /**
     * Find expensive products (top percentile)
     */
    @Query("SELECT p FROM Product p WHERE p.price IS NOT NULL ORDER BY p.price DESC")
    List<Product> findMostExpensiveProducts();

    /**
     * Find cheapest products
     */
    @Query("SELECT p FROM Product p WHERE p.price IS NOT NULL ORDER BY p.price ASC")
    List<Product> findCheapestProducts();
}