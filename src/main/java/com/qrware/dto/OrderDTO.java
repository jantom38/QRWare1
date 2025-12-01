package com.qrware.dto;

import com.qrware.domain.order.OrderStatus;
import com.qrware.domain.order.OrderType;
import com.qrware.domain.order.OrderPriority;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDTO {
    private Long id;
    private String orderNumber;
    private OrderType type;
    private OrderStatus status;
    private OrderPriority priority;
    private String description;
    
    // User information
    private Long createdById;
    private String createdByUsername;
    private String createdByFullName;
    private Long assignedToId;
    private String assignedToUsername;
    private String assignedToFullName;
    
    // Location information
    private Long sourceLocationId;
    private String sourceLocationName;
    private String sourceLocationCode;
    private Long destinationLocationId;
    private String destinationLocationName;
    private String destinationLocationCode;
    
    // Dates
    private LocalDateTime expectedDate;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional information
    private String cancellationReason;
    private Integer totalItems;
    private Integer completedItems;
    private BigDecimal estimatedValue;
    private String notes;
    private String externalReference;
    
    // Order items
    private List<OrderItemDTO> orderItems;
    
    // Calculated fields
    private Double completionPercentage;
    private Boolean isOverdue;
    private Boolean isHighPriority;
    private Boolean canBeStarted;
    private Boolean canBeCompleted;
    private Boolean canBeCancelled;
    private Boolean isActive;

    // Constructors
    public OrderDTO() {}

    public OrderDTO(Long id, String orderNumber, OrderType type, OrderStatus status) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.type = type;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public OrderType getType() { return type; }
    public void setType(OrderType type) { this.type = type; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public OrderPriority getPriority() { return priority; }
    public void setPriority(OrderPriority priority) { this.priority = priority; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getCreatedById() { return createdById; }
    public void setCreatedById(Long createdById) { this.createdById = createdById; }

    public String getCreatedByUsername() { return createdByUsername; }
    public void setCreatedByUsername(String createdByUsername) { this.createdByUsername = createdByUsername; }

    public String getCreatedByFullName() { return createdByFullName; }
    public void setCreatedByFullName(String createdByFullName) { this.createdByFullName = createdByFullName; }

    public Long getAssignedToId() { return assignedToId; }
    public void setAssignedToId(Long assignedToId) { this.assignedToId = assignedToId; }

    public String getAssignedToUsername() { return assignedToUsername; }
    public void setAssignedToUsername(String assignedToUsername) { this.assignedToUsername = assignedToUsername; }

    public String getAssignedToFullName() { return assignedToFullName; }
    public void setAssignedToFullName(String assignedToFullName) { this.assignedToFullName = assignedToFullName; }

    public Long getSourceLocationId() { return sourceLocationId; }
    public void setSourceLocationId(Long sourceLocationId) { this.sourceLocationId = sourceLocationId; }

    public String getSourceLocationName() { return sourceLocationName; }
    public void setSourceLocationName(String sourceLocationName) { this.sourceLocationName = sourceLocationName; }

    public String getSourceLocationCode() { return sourceLocationCode; }
    public void setSourceLocationCode(String sourceLocationCode) { this.sourceLocationCode = sourceLocationCode; }

    public Long getDestinationLocationId() { return destinationLocationId; }
    public void setDestinationLocationId(Long destinationLocationId) { this.destinationLocationId = destinationLocationId; }

    public String getDestinationLocationName() { return destinationLocationName; }
    public void setDestinationLocationName(String destinationLocationName) { this.destinationLocationName = destinationLocationName; }

    public String getDestinationLocationCode() { return destinationLocationCode; }
    public void setDestinationLocationCode(String destinationLocationCode) { this.destinationLocationCode = destinationLocationCode; }

    public LocalDateTime getExpectedDate() { return expectedDate; }
    public void setExpectedDate(LocalDateTime expectedDate) { this.expectedDate = expectedDate; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }

    public Integer getTotalItems() { return totalItems; }
    public void setTotalItems(Integer totalItems) { this.totalItems = totalItems; }

    public Integer getCompletedItems() { return completedItems; }
    public void setCompletedItems(Integer completedItems) { this.completedItems = completedItems; }

    public BigDecimal getEstimatedValue() { return estimatedValue; }
    public void setEstimatedValue(BigDecimal estimatedValue) { this.estimatedValue = estimatedValue; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getExternalReference() { return externalReference; }
    public void setExternalReference(String externalReference) { this.externalReference = externalReference; }

    public List<OrderItemDTO> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItemDTO> orderItems) { this.orderItems = orderItems; }

    public Double getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(Double completionPercentage) { this.completionPercentage = completionPercentage; }

    public Boolean getIsOverdue() { return isOverdue; }
    public void setIsOverdue(Boolean isOverdue) { this.isOverdue = isOverdue; }

    public Boolean getIsHighPriority() { return isHighPriority; }
    public void setIsHighPriority(Boolean isHighPriority) { this.isHighPriority = isHighPriority; }

    public Boolean getCanBeStarted() { return canBeStarted; }
    public void setCanBeStarted(Boolean canBeStarted) { this.canBeStarted = canBeStarted; }

    public Boolean getCanBeCompleted() { return canBeCompleted; }
    public void setCanBeCompleted(Boolean canBeCompleted) { this.canBeCompleted = canBeCompleted; }

    public Boolean getCanBeCancelled() { return canBeCancelled; }
    public void setCanBeCancelled(Boolean canBeCancelled) { this.canBeCancelled = canBeCancelled; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    @Override
    public String toString() {
        return "OrderDTO{" +
                "id=" + id +
                ", orderNumber='" + orderNumber + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", priority=" + priority +
                ", completionPercentage=" + completionPercentage +
                '}';
    }
}