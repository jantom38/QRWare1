package com.qrware.domain.order;

import com.qrware.domain.common.BaseEntity;
import com.qrware.domain.user.User;
import com.qrware.domain.warehouse.Location;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_order_number", columnList = "orderNumber"),
        @Index(name = "idx_order_status", columnList = "status"),
        @Index(name = "idx_order_type", columnList = "type"),
        @Index(name = "idx_order_creator", columnList = "creator_id"),
        @Index(name = "idx_order_assigned_to", columnList = "assignedTo"),
        @Index(name = "idx_order_expected_date", columnList = "expectedDate"),
        @Index(name = "idx_order_priority", columnList = "priority")
})
public class Order extends BaseEntity {

    @Column(unique = true, nullable = false, length = 50)
    @NotNull
    @Size(max = 50)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull
    private OrderType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @NotNull
    private OrderPriority priority = OrderPriority.NORMAL;

    @Size(max = 500)
    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    @NotNull
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_location_id")
    private Location sourceLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_location_id")
    private Location destinationLocation;

    @Column(name = "expected_date")
    private LocalDateTime expectedDate;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Size(max = 1000)
    @Column(name = "cancellation_reason", length = 1000)
    private String cancellationReason;

    @Column(name = "total_items")
    private Integer totalItems = 0;

    @Column(name = "completed_items")
    private Integer completedItems = 0;

    @Column(name = "estimated_value", precision = 15, scale = 2)
    private BigDecimal estimatedValue = BigDecimal.ZERO;

    @Size(max = 1000)
    @Column(name = "notes", length = 1000)
    private String notes;

    @Size(max = 200)
    @Column(name = "external_reference", length = 200)
    private String externalReference;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();

    public Order() {}

    public Order(String orderNumber, OrderType type, User creator) {
        this.orderNumber = orderNumber;
        this.type = type;
        this.creator = creator;
        this.status = OrderStatus.CREATED;
        this.priority = OrderPriority.NORMAL;
    }

    public boolean canBeStarted() {
        return status == OrderStatus.CREATED || status == OrderStatus.ASSIGNED;
    }

    public boolean canBeCompleted() {
        return status == OrderStatus.IN_PROGRESS &&
                completedItems != null && totalItems != null &&
                completedItems.equals(totalItems);
    }

    public boolean canBeCancelled() {
        return status != OrderStatus.COMPLETED && status != OrderStatus.CANCELLED;
    }

    public boolean isActive() {
        return status == OrderStatus.CREATED ||
                status == OrderStatus.ASSIGNED ||
                status == OrderStatus.IN_PROGRESS;
    }

    public void start(User user) {
        if (!canBeStarted()) {
            throw new IllegalStateException("Order cannot be started in current status: " + status);
        }
        this.status = OrderStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
        if (this.assignedTo == null) {
            this.assignedTo = user;
        }
    }

    public void complete(User user) {
        if (!canBeCompleted()) {
            throw new IllegalStateException("Order cannot be completed in current status: " + status);
        }
        this.status = OrderStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void cancel(User user, String reason) {
        if (!canBeCancelled()) {
            throw new IllegalStateException("Order cannot be cancelled in current status: " + status);
        }
        this.status = OrderStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
    }

    public void assign(User user) {
        this.assignedTo = user;
        if (this.status == OrderStatus.CREATED) {
            this.status = OrderStatus.ASSIGNED;
        }
    }

    public void updateProgress() {
        if (orderItems != null) {
            this.totalItems = orderItems.size();
            this.completedItems = (int) orderItems.stream()
                    .mapToLong(item -> item.getCompletedQuantity())
                    .count();

            this.estimatedValue = orderItems.stream()
                    .map(OrderItem::getTotalValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    public double getCompletionPercentage() {
        if (totalItems == null || totalItems == 0) {
            return 0.0;
        }
        return (double) (completedItems != null ? completedItems : 0) / totalItems * 100.0;
    }

    public boolean isOverdue() {
        return expectedDate != null &&
                LocalDateTime.now().isAfter(expectedDate) &&
                isActive();
    }

    public boolean isHighPriority() {
        return priority == OrderPriority.HIGH || priority == OrderPriority.URGENT;
    }

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

    public User getCreatorUser() { return creator; }
    public void setCreatorUser(User creator) { this.creator = creator; }

    public User getAssignedTo() { return assignedTo; }
    public void setAssignedTo(User assignedTo) { this.assignedTo = assignedTo; }

    public Location getSourceLocation() { return sourceLocation; }
    public void setSourceLocation(Location sourceLocation) { this.sourceLocation = sourceLocation; }

    public Location getDestinationLocation() { return destinationLocation; }
    public void setDestinationLocation(Location destinationLocation) { this.destinationLocation = destinationLocation; }

    public LocalDateTime getExpectedDate() { return expectedDate; }
    public void setExpectedDate(LocalDateTime expectedDate) { this.expectedDate = expectedDate; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }

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

    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }

    public List<OrderStatusHistory> getStatusHistory() { return statusHistory; }
    public void setStatusHistory(List<OrderStatusHistory> statusHistory) { this.statusHistory = statusHistory; }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + getId() +
                ", orderNumber='" + orderNumber + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", priority=" + priority +
                '}';
    }
}