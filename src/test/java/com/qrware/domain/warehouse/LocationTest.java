package com.qrware.domain.warehouse;

import com.qrware.domain.inventory.InventoryItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationTest {

    private Location location;

    @Mock
    private InventoryItem mockItem1;

    @Mock
    private InventoryItem mockItem2;

    @BeforeEach
    void setUp() {
        location = new Location();
        location.setCode("LOC-001");
        location.setInventoryItems(new ArrayList<>());
    }

    // ==================== CAPACITY TESTS ====================

    @Test
    @DisplayName("Should accept item when within volume capacity")
    void canAcceptItem_VolumeWithinLimit_ReturnsTrue() {
        // Given
        location.setCapacityVolume(new BigDecimal("100.0"));
        
        // Mock existing item taking 50.0 volume
        when(mockItem1.calculateTotalVolume()).thenReturn(new BigDecimal("50.0"));
        location.getInventoryItems().add(mockItem1);

        // When checking if we can add item with 40.0 volume (Total 90 <= 100)
        boolean result = location.canAcceptItem(new BigDecimal("40.0"), BigDecimal.ZERO);

        // Then
        assertTrue(result, "Should accept item when total volume is within capacity");
    }

    @Test
    @DisplayName("Should reject item when exceeding volume capacity")
    void canAcceptItem_VolumeExceedsLimit_ReturnsFalse() {
        // Given
        location.setCapacityVolume(new BigDecimal("100.0"));
        
        // Mock existing item taking 80.0 volume
        when(mockItem1.calculateTotalVolume()).thenReturn(new BigDecimal("80.0"));
        location.getInventoryItems().add(mockItem1);

        // When checking if we can add item with 30.0 volume (Total 110 > 100)
        boolean result = location.canAcceptItem(new BigDecimal("30.0"), BigDecimal.ZERO);

        // Then
        assertFalse(result, "Should reject item when total volume exceeds capacity");
    }

    @Test
    @DisplayName("Should accept item when within weight capacity")
    void canAcceptItem_WeightWithinLimit_ReturnsTrue() {
        // Given
        location.setCapacityWeight(new BigDecimal("1000.0"));
        
        // Mock existing item taking 500.0 weight
        when(mockItem1.calculateTotalWeight()).thenReturn(new BigDecimal("500.0"));
        location.getInventoryItems().add(mockItem1);

        // When checking if we can add item with 400.0 weight
        boolean result = location.canAcceptItem(BigDecimal.ZERO, new BigDecimal("400.0"));

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("Should reject item when exceeding weight capacity")
    void canAcceptItem_WeightExceedsLimit_ReturnsFalse() {
        // Given
        location.setCapacityWeight(new BigDecimal("1000.0"));
        
        // Mock existing item taking 900.0 weight
        when(mockItem1.calculateTotalWeight()).thenReturn(new BigDecimal("900.0"));
        location.getInventoryItems().add(mockItem1);

        // When checking if we can add item with 200.0 weight
        boolean result = location.canAcceptItem(BigDecimal.ZERO, new BigDecimal("200.0"));

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should reject item when item count limit reached")
    void canAcceptItem_ItemCountLimitReached_ReturnsFalse() {
        // Given
        location.setCapacityItems(2);
        location.getInventoryItems().add(mockItem1);
        location.getInventoryItems().add(mockItem2);

        // When trying to add a 3rd item
        boolean result = location.canAcceptItem(BigDecimal.ONE, BigDecimal.ONE);

        // Then
        assertFalse(result, "Should reject when max item count is reached");
    }

    @Test
    @DisplayName("Should allow unlimited items if capacities are null")
    void canAcceptItem_NoLimitsSet_ReturnsTrue() {
        // Given
        location.setCapacityVolume(null);
        location.setCapacityWeight(null);
        location.setCapacityItems(null);

        // When
        boolean result = location.canAcceptItem(new BigDecimal("99999"), new BigDecimal("99999"));

        // Then
        assertTrue(result);
    }

    // ==================== UTILIZATION TESTS ====================

    @Test
    @DisplayName("Should calculate volume utilization correctly")
    void getVolumeUtilization_CalculatesCorrectly() {
        // Given
        location.setCapacityVolume(new BigDecimal("200.0"));
        when(mockItem1.calculateTotalVolume()).thenReturn(new BigDecimal("50.0")); // 25%
        when(mockItem2.calculateTotalVolume()).thenReturn(new BigDecimal("50.0")); // 25%
        location.getInventoryItems().addAll(List.of(mockItem1, mockItem2));

        // When
        BigDecimal utilization = location.getVolumeUtilization();

        // Then (0.50)
        assertEquals(0, new BigDecimal("0.5000").compareTo(utilization));
    }

    @Test
    @DisplayName("Should return zero utilization when capacity is zero/null")
    void getVolumeUtilization_ZeroCapacity_ReturnsZero() {
        // Given
        location.setCapacityVolume(BigDecimal.ZERO);

        // When
        BigDecimal utilization = location.getVolumeUtilization();

        // Then
        assertEquals(BigDecimal.ZERO, utilization);
    }

    // ==================== ENVIRONMENTAL TESTS ====================

    @Test
    @DisplayName("Should validate temperature range correctly")
    void isTemperatureInRange_ValidatesCorrectly() {
        // Given
        location.setTemperatureControlled(true);
        location.setTemperatureMin(10);
        location.setTemperatureMax(20);

        // Then
        assertTrue(location.isTemperatureInRange(15), "15 should be in range [10, 20]");
        assertTrue(location.isTemperatureInRange(10), "10 (min) should be in range");
        assertTrue(location.isTemperatureInRange(20), "20 (max) should be in range");
        
        assertFalse(location.isTemperatureInRange(5), "5 should be below range");
        assertFalse(location.isTemperatureInRange(25), "25 should be above range");
    }

    @Test
    @DisplayName("Should always return true if not temperature controlled")
    void isTemperatureInRange_NotControlled_ReturnsTrue() {
        // Given
        location.setTemperatureControlled(false);
        location.setTemperatureMin(10);
        location.setTemperatureMax(20);

        // Then
        assertTrue(location.isTemperatureInRange(100), "Should ignore limits if control is disabled");
    }

    @Test
    @DisplayName("Should validate humidity range correctly")
    void isHumidityInRange_ValidatesCorrectly() {
        // Given
        location.setHumidityControlled(true);
        location.setHumidityMin(40);
        location.setHumidityMax(60);

        // Then
        assertTrue(location.isHumidityInRange(50));
        assertFalse(location.isHumidityInRange(30));
        assertFalse(location.isHumidityInRange(70));
    }

    // ==================== ADDRESS TESTS ====================

    @Test
    @DisplayName("Should format full address correctly")
    void getFullAddress_FormatsCorrectly() {
        // Given
        location.setAisle("01");
        location.setRack("02");
        location.setShelf("03");
        location.setBin("04");

        // When
        String address = location.getFullAddress();

        // Then
        assertEquals("A01-R02-S03-B04", address);
    }

    @Test
    @DisplayName("Should format partial address correctly")
    void getFullAddress_PartialFields_FormatsCorrectly() {
        // Given
        location.setAisle("01");
        location.setRack(null);
        location.setShelf("03");
        location.setBin(null);

        // When
        String address = location.getFullAddress();

        // Then
        assertEquals("A01-S03", address);
    }
}
