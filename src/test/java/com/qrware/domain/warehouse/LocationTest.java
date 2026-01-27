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

    @Test
    @DisplayName("Should accept item when within volume capacity")
    void canAcceptItem_VolumeWithinLimit_ReturnsTrue() {
        location.setCapacityVolume(new BigDecimal("100.0"));
        
        when(mockItem1.calculateTotalVolume()).thenReturn(new BigDecimal("50.0"));
        location.getInventoryItems().add(mockItem1);

        boolean result = location.canAcceptItem(new BigDecimal("40.0"), BigDecimal.ZERO);

        assertTrue(result, "Should accept item when total volume is within capacity");
    }

    @Test
    @DisplayName("Should reject item when exceeding volume capacity")
    void canAcceptItem_VolumeExceedsLimit_ReturnsFalse() {
        location.setCapacityVolume(new BigDecimal("100.0"));
        
        when(mockItem1.calculateTotalVolume()).thenReturn(new BigDecimal("80.0"));
        location.getInventoryItems().add(mockItem1);

        boolean result = location.canAcceptItem(new BigDecimal("30.0"), BigDecimal.ZERO);

        assertFalse(result, "Should reject item when total volume exceeds capacity");
    }

    @Test
    @DisplayName("Should accept item when within weight capacity")
    void canAcceptItem_WeightWithinLimit_ReturnsTrue() {
        location.setCapacityWeight(new BigDecimal("1000.0"));
        
        when(mockItem1.calculateTotalWeight()).thenReturn(new BigDecimal("500.0"));
        location.getInventoryItems().add(mockItem1);

        boolean result = location.canAcceptItem(BigDecimal.ZERO, new BigDecimal("400.0"));

        assertTrue(result);
    }

    @Test
    @DisplayName("Should reject item when exceeding weight capacity")
    void canAcceptItem_WeightExceedsLimit_ReturnsFalse() {
        location.setCapacityWeight(new BigDecimal("1000.0"));
        
        when(mockItem1.calculateTotalWeight()).thenReturn(new BigDecimal("900.0"));
        location.getInventoryItems().add(mockItem1);

        boolean result = location.canAcceptItem(BigDecimal.ZERO, new BigDecimal("200.0"));

        assertFalse(result);
    }

    @Test
    @DisplayName("Should reject item when item count limit reached")
    void canAcceptItem_ItemCountLimitReached_ReturnsFalse() {
        location.setCapacityItems(2);
        location.getInventoryItems().add(mockItem1);
        location.getInventoryItems().add(mockItem2);

        boolean result = location.canAcceptItem(BigDecimal.ONE, BigDecimal.ONE);

        assertFalse(result, "Should reject when max item count is reached");
    }

    @Test
    @DisplayName("Should allow unlimited items if capacities are null")
    void canAcceptItem_NoLimitsSet_ReturnsTrue() {
        location.setCapacityVolume(null);
        location.setCapacityWeight(null);
        location.setCapacityItems(null);

        boolean result = location.canAcceptItem(new BigDecimal("99999"), new BigDecimal("99999"));

        assertTrue(result);
    }

    @Test
    @DisplayName("Should calculate volume utilization correctly")
    void getVolumeUtilization_CalculatesCorrectly() {
        location.setCapacityVolume(new BigDecimal("200.0"));
        when(mockItem1.calculateTotalVolume()).thenReturn(new BigDecimal("50.0")); 
        when(mockItem2.calculateTotalVolume()).thenReturn(new BigDecimal("50.0")); 
        location.getInventoryItems().addAll(List.of(mockItem1, mockItem2));

        BigDecimal utilization = location.getVolumeUtilization();

        assertEquals(0, new BigDecimal("0.5000").compareTo(utilization));
    }

    @Test
    @DisplayName("Should return zero utilization when capacity is zero/null")
    void getVolumeUtilization_ZeroCapacity_ReturnsZero() {
        location.setCapacityVolume(BigDecimal.ZERO);

        BigDecimal utilization = location.getVolumeUtilization();

        assertEquals(BigDecimal.ZERO, utilization);
    }

    @Test
    @DisplayName("Should validate temperature range correctly")
    void isTemperatureInRange_ValidatesCorrectly() {
        location.setTemperatureControlled(true);
        location.setTemperatureMin(10);
        location.setTemperatureMax(20);

        assertTrue(location.isTemperatureInRange(15), "15 should be in range [10, 20]");
        assertTrue(location.isTemperatureInRange(10), "10 (min) should be in range");
        assertTrue(location.isTemperatureInRange(20), "20 (max) should be in range");
        
        assertFalse(location.isTemperatureInRange(5), "5 should be below range");
        assertFalse(location.isTemperatureInRange(25), "25 should be above range");
    }

    @Test
    @DisplayName("Should always return true if not temperature controlled")
    void isTemperatureInRange_NotControlled_ReturnsTrue() {
        location.setTemperatureControlled(false);
        location.setTemperatureMin(10);
        location.setTemperatureMax(20);

        assertTrue(location.isTemperatureInRange(100), "Should ignore limits if control is disabled");
    }

    @Test
    @DisplayName("Should validate humidity range correctly")
    void isHumidityInRange_ValidatesCorrectly() {
        location.setHumidityControlled(true);
        location.setHumidityMin(40);
        location.setHumidityMax(60);

        assertTrue(location.isHumidityInRange(50));
        assertFalse(location.isHumidityInRange(30));
        assertFalse(location.isHumidityInRange(70));
    }

    @Test
    @DisplayName("Should format full address correctly")
    void getFullAddress_FormatsCorrectly() {
        location.setAisle("01");
        location.setRack("02");
        location.setShelf("03");
        location.setBin("04");

        String address = location.getFullAddress();

        assertEquals("A01-R02-S03-B04", address);
    }

    @Test
    @DisplayName("Should format partial address correctly")
    void getFullAddress_PartialFields_FormatsCorrectly() {
        location.setAisle("01");
        location.setRack(null);
        location.setShelf("03");
        location.setBin(null);

        String address = location.getFullAddress();

        assertEquals("A01-S03", address);
    }
}
