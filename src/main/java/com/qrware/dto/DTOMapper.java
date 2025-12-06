package com.qrware.dto;

import com.qrware.domain.inventory.InventoryItem;
import com.qrware.domain.inventory.MovementHistory;
import com.qrware.domain.product.Product;
import com.qrware.domain.product.Category;
import com.qrware.domain.warehouse.Location;
import com.qrware.domain.warehouse.Zone;
import com.qrware.domain.qr.QRCodeData;
import com.qrware.domain.order.Order;
import com.qrware.domain.order.OrderItem;
import com.qrware.domain.order.OrderItemStatus;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
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
        dto.setIcon(category.getIcon());
        dto.setColor(category.getColor());
        dto.setRequiresSpecialHandling(category.getRequiresSpecialHandling());
        dto.setStorageTemperatureMin(category.getStorageTemperatureMin());
        dto.setStorageTemperatureMax(category.getStorageTemperatureMax());
        dto.setStorageHumidityMin(category.getStorageHumidityMin());
        dto.setStorageHumidityMax(category.getStorageHumidityMax());
        dto.setLevel(category.getLevel());
        dto.setFullPath(category.getFullPath());

        if (category.getParent() != null) {
            CategoryDTO parentDTO = new CategoryDTO();
            parentDTO.setId(category.getParent().getId());
            parentDTO.setName(category.getParent().getName());
            parentDTO.setCode(category.getParent().getCode());
            dto.setParent(parentDTO);
        }

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
        dto.setActive(location.getActive());

        dto.setZone(toDTO(location.getZone()));

        return dto;
    }

    public ZoneDTO toDTO(Zone zone) {
        if (zone == null) {
            return null;
        }

        return new ZoneDTO(
                zone.getId(),
                zone.getName(),
                zone.getCode(),
                zone.getDescription(),
                zone.getType(),
                zone.getActive(),
                zone.getTemperatureControlled(),
                zone.getTemperatureMin(),
                zone.getTemperatureMax(),
                zone.getHumidityControlled(),
                zone.getHumidityMin(),
                zone.getHumidityMax(),
                zone.getSecurityLevel(),
                zone.getHazardousMaterials(),
                zone.getFragileItems(),
                zone.getPickingPriority(),
                zone.getManager(),
                zone.getContactInfo(),
                zone.getColor(),
                zone.getCreatedAt(),
                zone.getUpdatedAt(),
                zone.getCreatedBy(),
                zone.getUpdatedBy(),
                zone.getLocationCount(),
                zone.getActiveLocationCount(),
                zone.getOccupiedLocationCount(),
                zone.getOccupancyRate()
        );
    }

    public QRCodeDTO toDTO(QRCodeData qrCode) {
        if (qrCode == null) return null;

        QRCodeDTO dto = new QRCodeDTO();
        dto.setId(qrCode.getId());
        dto.setCode(qrCode.getCode());
        dto.setType(qrCode.getType());
        dto.setEntityType(qrCode.getEntityType());
        dto.setEntityId(qrCode.getEntityId());
        dto.setData(qrCode.getData());
        dto.setMetadata(qrCode.getMetadata());
        dto.setActive(qrCode.getActive());
        dto.setExpiresAt(qrCode.getExpiresAt());
        dto.setLastScanned(qrCode.getLastScanned());
        dto.setScanCount(qrCode.getScanCount());
        dto.setFormat(qrCode.getFormat());
        dto.setSize(qrCode.getSize());
        dto.setErrorCorrectionLevel(qrCode.getErrorCorrectionLevel());
        dto.setGeneratedBy(qrCode.getGeneratedBy());
        dto.setGenerationReason(qrCode.getGenerationReason());
        dto.setImagePath(qrCode.getImagePath());

        return dto;
    }

    public MovementHistoryDTO toMovementHistoryDTO(MovementHistory movement) {
        if (movement == null) return null;

        MovementHistoryDTO dto = new MovementHistoryDTO();
        dto.setId(movement.getId());
        dto.setInventoryItem(toInventoryItemDTO(movement.getInventoryItem()));
        dto.setMovementType(movement.getMovementType().name());
        dto.setMovementDate(movement.getMovementDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        dto.setQuantityBefore(movement.getQuantityBefore());
        dto.setQuantityAfter(movement.getQuantityAfter());
        dto.setQuantityChanged(movement.getQuantityChanged());
        dto.setFromLocation(movement.getFromLocation() != null ? toLocationDTO(movement.getFromLocation()) : null);
        dto.setToLocation(movement.getToLocation() != null ? toLocationDTO(movement.getToLocation()) : null);
        dto.setStatusBefore(movement.getStatusBefore());
        dto.setStatusAfter(movement.getStatusAfter());
        dto.setUnitCost(movement.getUnitCost());
        dto.setTotalCost(movement.getTotalCost());
        dto.setReferenceNumber(movement.getReferenceNumber());
        dto.setReferenceType(movement.getReferenceType());
        dto.setReason(movement.getReason());
        dto.setNotes(movement.getNotes());
        dto.setUserId(movement.getUserId());
        dto.setUserName(movement.getUserName());
        dto.setApproved(movement.getApproved());
        dto.setApprovedBy(movement.getApprovedBy());
        dto.setApprovedDate(movement.getApprovedDate() != null ?
                movement.getApprovedDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
        dto.setBatchId(movement.getBatchId());
        dto.setSystemGenerated(movement.getSystemGenerated());
        dto.setTemperature(movement.getTemperature());
        dto.setHumidity(movement.getHumidity());
        dto.setWeight(movement.getWeight());
        dto.setVolume(movement.getVolume());
        dto.setCreatedAt(movement.getCreatedAt() != null ?
                movement.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
        dto.setUpdatedAt(movement.getUpdatedAt() != null ?
                movement.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);

        return dto;
    }

    public InventoryItemDTO toInventoryItemDTO(InventoryItem item) {
        return toDTO(item);
    }

    public LocationDTO toLocationDTO(Location location) {
        return toDTO(location);
    }

    public OrderDTO toOrderDTO(Order order) {
        if (order == null) return null;

        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setType(order.getType());
        dto.setStatus(order.getStatus());
        dto.setPriority(order.getPriority());
        dto.setDescription(order.getDescription());

        if (order.getCreatedBy() != null) {
            dto.setCreatedById(order.getCreatorUser().getId());
            dto.setCreatedByUsername(order.getCreatorUser().getUsername());
            dto.setCreatedByFullName(order.getCreatorUser().getFullName());
        }

        if (order.getAssignedTo() != null) {
            dto.setAssignedToId(order.getAssignedTo().getId());
            dto.setAssignedToUsername(order.getAssignedTo().getUsername());
            dto.setAssignedToFullName(order.getAssignedTo().getFullName());
        }

        if (order.getSourceLocation() != null) {
            dto.setSourceLocationId(order.getSourceLocation().getId());
            dto.setSourceLocationName(order.getSourceLocation().getName());
            dto.setSourceLocationCode(order.getSourceLocation().getCode());
        }

        if (order.getDestinationLocation() != null) {
            dto.setDestinationLocationId(order.getDestinationLocation().getId());
            dto.setDestinationLocationName(order.getDestinationLocation().getName());
            dto.setDestinationLocationCode(order.getDestinationLocation().getCode());
        }

        dto.setExpectedDate(order.getExpectedDate());
        dto.setStartedAt(order.getStartedAt());
        dto.setCompletedAt(order.getCompletedAt());
        dto.setCancelledAt(order.getCancelledAt());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        dto.setCancellationReason(order.getCancellationReason());
        dto.setTotalItems(order.getTotalItems());
        dto.setCompletedItems(order.getCompletedItems());
        dto.setEstimatedValue(order.getEstimatedValue());
        dto.setNotes(order.getNotes());
        dto.setExternalReference(order.getExternalReference());

        if (order.getOrderItems() != null) {
            dto.setOrderItems(order.getOrderItems().stream()
                    .map(this::toOrderItemDTO)
                    .collect(Collectors.toList()));
        }

        dto.setCompletionPercentage(order.getCompletionPercentage());
        dto.setIsOverdue(order.isOverdue());
        dto.setIsHighPriority(order.isHighPriority());
        dto.setCanBeStarted(order.canBeStarted());
        dto.setCanBeCompleted(order.canBeCompleted());
        dto.setCanBeCancelled(order.canBeCancelled());
        dto.setIsActive(order.isActive());

        return dto;
    }

    public OrderItemDTO toOrderItemDTO(OrderItem orderItem) {
        if (orderItem == null) return null;

        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(orderItem.getId());
        dto.setLineNumber(orderItem.getLineNumber());

        if (orderItem.getOrder() != null) {
            dto.setOrderId(orderItem.getOrder().getId());
            dto.setOrderNumber(orderItem.getOrder().getOrderNumber());
        }

        if (orderItem.getProduct() != null) {
            dto.setProductId(orderItem.getProduct().getId());
            dto.setProductName(orderItem.getProduct().getName());
            dto.setProductSku(orderItem.getProduct().getSku());
            dto.setProductDescription(orderItem.getProduct().getDescription());
        }

        if (orderItem.getInventoryItem() != null) {
            dto.setInventoryItemId(orderItem.getInventoryItem().getId());
            dto.setInventoryItemCode(orderItem.getInventoryItem().getQrCode());
        }

        if (orderItem.getSourceLocation() != null) {
            dto.setSourceLocationId(orderItem.getSourceLocation().getId());
            dto.setSourceLocationName(orderItem.getSourceLocation().getName());
            dto.setSourceLocationCode(orderItem.getSourceLocation().getCode());
        }

        if (orderItem.getDestinationLocation() != null) {
            dto.setDestinationLocationId(orderItem.getDestinationLocation().getId());
            dto.setDestinationLocationName(orderItem.getDestinationLocation().getName());
            dto.setDestinationLocationCode(orderItem.getDestinationLocation().getCode());
        }

        dto.setRequestedQuantity(orderItem.getRequestedQuantity());
        dto.setCompletedQuantity(orderItem.getCompletedQuantity());
        dto.setRemainingQuantity(orderItem.getRemainingQuantity());
        dto.setUnitPrice(orderItem.getUnitPrice());
        dto.setTotalValue(orderItem.getTotalValue());

        dto.setStatus(orderItem.getStatus() != null ? orderItem.getStatus() : OrderItemStatus.PENDING);
        dto.setNotes(orderItem.getNotes());
        dto.setBatchNumber(orderItem.getBatchNumber());
        dto.setSerialNumber(orderItem.getSerialNumber());
        dto.setQrCodeData(orderItem.getQrCodeData());

        dto.setExpiryDate(orderItem.getExpiryDate());
        dto.setPickedAt(orderItem.getPickedAt());
        dto.setCompletedAt(orderItem.getCompletedAt());
        dto.setCreatedAt(orderItem.getCreatedAt());
        dto.setUpdatedAt(orderItem.getUpdatedAt());

        dto.setCompletionNotes(orderItem.getCompletionNotes());

        dto.setCompletionPercentage(orderItem.getCompletionPercentage());
        dto.setIsCompleted(orderItem.isCompleted());
        dto.setIsPartiallyCompleted(orderItem.isPartiallyCompleted());
        dto.setCanBeCompleted(orderItem.canBeCompleted());
        dto.setRequiresQRScan(orderItem.requiresQRScan());
        dto.setIsQRScanned(orderItem.isQRScanned());
        
        dto.setRequiresExactInventory(orderItem.getRequiresExactInventory());
        dto.setActualSourceQrCode(orderItem.getActualSourceQrCode());
        dto.setFulfillmentNotes(orderItem.getFulfillmentNotes());

        return dto;
    }
}
