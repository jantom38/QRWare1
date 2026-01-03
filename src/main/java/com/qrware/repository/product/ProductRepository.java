package com.qrware.repository.product;

import com.qrware.domain.product.Product;
import com.qrware.dto.LowStockReportDTO;
import com.qrware.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends BaseRepository<Product> {

    Optional<Product> findBySku(String sku);

    Optional<Product> findBySkuIgnoreCase(String sku);

    Optional<Product> findByBarcode(String barcode);

    boolean existsBySku(String sku);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Product p WHERE p.sku = :sku AND p.id != :productId")
    boolean existsBySkuAndIdNot(@Param("sku") String sku, @Param("productId") Long productId);

    boolean existsByBarcode(String barcode);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Product p WHERE p.barcode = :barcode AND p.id != :productId")
    boolean existsByBarcodeAndIdNot(@Param("barcode") String barcode, @Param("productId") Long productId);

    List<Product> findByActiveTrue();

    List<Product> findByActiveFalse();

    Page<Product> findByActive(Boolean active, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId")
    List<Product> findByCategoryId(@Param("categoryId") Long categoryId);

    @Query("SELECT p FROM Product p WHERE p.category.name = :categoryName")
    List<Product> findByCategoryName(@Param("categoryName") String categoryName);

    @Query("SELECT p FROM Product p WHERE p.category.code = :categoryCode")
    List<Product> findByCategoryCode(@Param("categoryCode") String categoryCode);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> findByNameContainingIgnoreCase(@Param("keyword") String keyword);

    @Query("SELECT p FROM Product p WHERE LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> findByDescriptionContainingIgnoreCase(@Param("keyword") String keyword);

    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Product> searchProducts(@Param("searchTerm") String searchTerm);

    @Query("SELECT p FROM Product p WHERE LOWER(p.manufacturer) LIKE LOWER(CONCAT('%', :manufacturer, '%'))")
    List<Product> findByManufacturerContainingIgnoreCase(@Param("manufacturer") String manufacturer);

    @Query("SELECT p FROM Product p WHERE LOWER(p.supplier) LIKE LOWER(CONCAT('%', :supplier, '%'))")
    List<Product> findBySupplierContainingIgnoreCase(@Param("supplier") String supplier);

    List<Product> findByUnitOfMeasure(String unitOfMeasure);

    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    List<Product> findByPriceBetween(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT p FROM Product p WHERE p.cost BETWEEN :minCost AND :maxCost")
    List<Product> findByCostBetween(@Param("minCost") BigDecimal minCost, @Param("maxCost") BigDecimal maxCost);

    @Query("SELECT p FROM Product p WHERE p.weight BETWEEN :minWeight AND :maxWeight")
    List<Product> findByWeightBetween(@Param("minWeight") BigDecimal minWeight, @Param("maxWeight") BigDecimal maxWeight);

    List<Product> findByPerishableTrue();

    List<Product> findByPerishableFalse();

    List<Product> findByHazardousTrue();

    List<Product> findByHazardousFalse();

    List<Product> findByFragileTrue();

    List<Product> findByFragileFalse();

    @Query("SELECT p FROM Product p WHERE p.perishable = true OR p.hazardous = true OR p.fragile = true")
    List<Product> findProductsRequiringSpecialHandling();

    @Query("SELECT p FROM Product p WHERE p.minimumStock <= :threshold")
    List<Product> findByMinimumStockLessThanEqual(@Param("threshold") Integer threshold);

    @Query("SELECT p FROM Product p WHERE p.minimumStock >= :threshold")
    List<Product> findByMinimumStockGreaterThanEqual(@Param("threshold") Integer threshold);

    @Query("SELECT p FROM Product p WHERE p.reorderPoint IS NOT NULL")
    List<Product> findProductsWithReorderPoint();

    @Query("SELECT p FROM Product p WHERE p.reorderPoint IS NULL")
    List<Product> findProductsWithoutReorderPoint();

    @Query("SELECT p FROM Product p WHERE p.maximumStock IS NOT NULL")
    List<Product> findProductsWithMaximumStock();

    @Query("SELECT p FROM Product p WHERE p.maximumStock IS NULL")
    List<Product> findProductsWithoutMaximumStock();

    @Query("SELECT p FROM Product p WHERE LOWER(p.storageConditions) LIKE LOWER(CONCAT('%', :condition, '%'))")
    List<Product> findByStorageConditionsContaining(@Param("condition") String condition);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    long countByCategoryId(@Param("categoryId") Long categoryId);

    long countByActiveTrue();

    long countByActiveFalse();

    long countByPerishableTrue();

    long countByHazardousTrue();

    long countByFragileTrue();

    @Query("SELECT " +
           "COUNT(p) as totalProducts, " +
           "SUM(CASE WHEN p.active = true THEN 1 ELSE 0 END) as activeProducts, " +
           "SUM(CASE WHEN p.perishable = true THEN 1 ELSE 0 END) as perishableProducts, " +
           "SUM(CASE WHEN p.hazardous = true THEN 1 ELSE 0 END) as hazardousProducts, " +
           "SUM(CASE WHEN p.fragile = true THEN 1 ELSE 0 END) as fragileProducts " +
           "FROM Product p")
    Object[] getProductStatistics();

    @Query("SELECT p, SIZE(p.inventoryItems) as inventoryCount FROM Product p ORDER BY SIZE(p.inventoryItems) DESC")
    List<Object[]> getProductsWithInventoryCount();

    @Query("SELECT p FROM Product p WHERE SIZE(p.inventoryItems) = 0")
    List<Product> findProductsWithoutInventory();

    @Query("SELECT DISTINCT p FROM Product p WHERE SIZE(p.inventoryItems) > 0")
    List<Product> findProductsWithInventory();

    @Query("SELECT p FROM Product p WHERE p.price > (SELECT AVG(p2.price) FROM Product p2 WHERE p2.price IS NOT NULL)")
    List<Product> findProductsWithPriceAboveAverage();

    @Query("SELECT p FROM Product p WHERE p.cost > (SELECT AVG(p2.cost) FROM Product p2 WHERE p2.cost IS NOT NULL)")
    List<Product> findProductsWithCostAboveAverage();

    @Query("SELECT DISTINCT p.manufacturer FROM Product p WHERE p.manufacturer IS NOT NULL ORDER BY p.manufacturer")
    List<String> findAllManufacturers();

    @Query("SELECT DISTINCT p.supplier FROM Product p WHERE p.supplier IS NOT NULL ORDER BY p.supplier")
    List<String> findAllSuppliers();

    @Query("SELECT DISTINCT p.unitOfMeasure FROM Product p ORDER BY p.unitOfMeasure")
    List<String> findAllUnitsOfMeasure();

    @Query("SELECT p FROM Product p WHERE p.createdBy = :username")
    List<Product> findCreatedBy(@Param("username") String username);

    @Query("SELECT p FROM Product p ORDER BY p.createdAt DESC")
    List<Product> findMostRecentlyCreated();

    @Query("SELECT p FROM Product p ORDER BY p.updatedAt DESC")
    List<Product> findMostRecentlyUpdated();

    @Query("SELECT p FROM Product p WHERE p.price IS NOT NULL AND p.cost IS NOT NULL ORDER BY (p.price - p.cost) DESC")
    List<Product> findProductsWithHighestMargin();

    @Query("SELECT p FROM Product p WHERE p.price IS NOT NULL AND p.cost IS NOT NULL ORDER BY (p.price - p.cost) ASC")
    List<Product> findProductsWithLowestMargin();

    @Query("SELECT p FROM Product p WHERE p.price IS NOT NULL ORDER BY p.price DESC")
    List<Product> findMostExpensiveProducts();

    @Query("SELECT p FROM Product p WHERE p.price IS NOT NULL ORDER BY p.price ASC")
    List<Product> findCheapestProducts();

    @Query("SELECT new com.qrware.dto.LowStockReportDTO(p.id, p.sku, p.name, COALESCE(SUM(i.quantity), 0), p.minimumStock, p.reorderPoint) " +
           "FROM Product p " +
           "LEFT JOIN p.inventoryItems i " +
           "WHERE p.active = true " +
           "GROUP BY p.id, p.sku, p.name, p.minimumStock, p.reorderPoint " +
           "HAVING (COALESCE(SUM(i.quantity), 0) <= p.minimumStock) " +
           "OR (p.reorderPoint IS NOT NULL AND COALESCE(SUM(i.quantity), 0) <= p.reorderPoint)")
    List<LowStockReportDTO> findLowStockProducts();
}