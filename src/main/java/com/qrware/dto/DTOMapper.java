package com.qrware.dto;

import com.qrware.domain.inventory.InventoryItem;
import com.qrware.domain.product.Product;
import com.qrware.domain.product.Category;
import com.qrware.domain.warehouse.Location;
import com.qrware.domain.warehouse.Zone;
import org.springframework.stereotype.Component;

@Component
public class DTOMapper {

    public InventoryItemDTO toDTO(InventoryItem item) {
        if (item == null) return null;
        
        InventoryItemDTO dto = new InventoryItemDTO();
        dto.setId(item.getId());
        dto.setQuantity(item.getQuantity());
        dto.setReservedQuantity(item.getReservedQuantity());
        dto.setAvailableQuantity(item.getAvailableQuantity());
        dto.setStatus(item.getStatus());
        dto.setQrCode(item.getQrCode());
        dto.setLotNumber(item.getLotNumber());
        dto.setBatchNumber(item.getBatchNumber());
        dto.setSerialNumber(item.getSerialNumber());
        dto.setReceivedDate(item.getReceivedDate());
        dto.setExpiryDate(item.getExpiryDate());
        dto.setUnitCost(item.getUnitCost());
        dto.setTotalCost(item.getTotalCost());
        dto.setNotes(item.getNotes());
        
        dto.setProduct(toDTO(item.getProduct()));
        dto.setLocation(toDTO(item.getLocation()));
        
        return dto;
    }

    public ProductDTO toDTO(Product product) {
        if (product == null) return null;
        
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setSku(product.getSku());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setWeight(product.getWeight());
        dto.setDimensionsLength(product.getDimensionsLength());
        dto.setDimensionsWidth(product.getDimensionsWidth());
        dto.setDimensionsHeight(product.getDimensionsHeight());
        dto.setUnitOfMeasure(product.getUnitOfMeasure());
        dto.setMinimumStock(product.getMinimumStock());
        dto.setMaximumStock(product.getMaximumStock());
        dto.setReorderPoint(product.getReorderPoint());
        dto.setActive(product.getActive());
        
        dto.setCategory(toDTO(product.getCategory()));
        
        return dto;
    }

    public CategoryDTO toDTO(Category category) {
        if (category == null) return null;
        
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setCode(category.getCode());
        dto.setDescription(category.getDescription());
        dto.setActive(category.getActive());
        dto.setSortOrder(category.getSortOrder());
        
        return dto;
    }

    public LocationDTO toDTO(Location location) {
        if (location == null) return null;
        
        LocationDTO dto = new LocationDTO();
        dto.setId(location.getId());
        dto.setCode(location.getCode());
        dto.setName(location.getName());
        dto.setDescription(location.getDescription());
        dto.setAisle(location.getAisle());
        dto.setRack(location.getRack());
        dto.setShelf(location.getShelf());
        dto.setBarcode(location.getBarcode());
        
        dto.setZone(toDTO(location.getZone()));
        
        return dto;
    }

    public ZoneDTO toDTO(Zone zone) {
        if (zone == null) return null;
        
        ZoneDTO dto = new ZoneDTO();
        dto.setId(zone.getId());
        dto.setCode(zone.getCode());
        dto.setName(zone.getName());
        dto.setDescription(zone.getDescription());
        dto.setType(zone.getType());
        dto.setColor(zone.getColor());
        
        return dto;
    }
}