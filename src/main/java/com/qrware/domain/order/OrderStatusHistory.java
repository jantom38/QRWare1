package com.qrware.domain.order;

import com.qrware.domain.common.BaseEntity;
import com.qrware.domain.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_status_history", indexes = {
    @Index(name = "idx_order_status_history_order", columnList = "order_id"),
    @Index(name = "idx_order_status_history_user", columnList = "changed_by"),
    @Index(name = "idx_order_status_history_date", columnList = "changed_at"),
    @Index(name = "idx_order_status_history_status", columnList = "new_status")
})
public class OrderStatusHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    @NotNull
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 20)
    private OrderStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 20)
    @NotNull
    private OrderStatus newStatus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "changed_by", nullable = false)
    @NotNull
    private User changedBy;

    @Column(name = "changed_at", nullable = false)
    @NotNull
    private LocalDateTime changedAt;

    @Size(max = 1000)
    @Column(name = "reason", length = 1000)
    private String reason;

    @Size(max = 1000)
    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "system_generated")
    private Boolean systemGenerated = false;

    public OrderStatusHistory() {}

    public OrderStatusHistory(Order order, OrderStatus oldStatus, OrderStatus newStatus, User changedBy) {
        this.order = order;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
        this.changedAt = LocalDateTime.now();
        this.systemGenerated = false;
    }

    public OrderStatusHistory(Order order, OrderStatus oldStatus, OrderStatus newStatus, User changedBy, String reason) {
        this(order, oldStatus, newStatus, changedBy);
        this.reason = reason;
    }

    public static OrderStatusHistory createSystemChange(Order order, OrderStatus oldStatus, 
                                                       OrderStatus newStatus, User systemUser, String reason) {
        OrderStatusHistory history = new OrderStatusHistory(order, oldStatus, newStatus, systemUser, reason);
        history.systemGenerated = true;
        return history;
    }

    public static OrderStatusHistory createUserChange(Order order, OrderStatus oldStatus, 
                                                     OrderStatus newStatus, User user, String reason, String notes) {
        OrderStatusHistory history = new OrderStatusHistory(order, oldStatus, newStatus, user, reason);
        history.notes = notes;
        return history;
    }

    public boolean isStatusUpgrade() {
        if (oldStatus == null) {
            return true;
        }
        
        return getStatusLevel(newStatus) > getStatusLevel(oldStatus);
    }

    public boolean isStatusDowngrade() {
        if (oldStatus == null) {
            return false;
        }
        return getStatusLevel(newStatus) < getStatusLevel(oldStatus);
    }

    public boolean isCancellation() {
        return newStatus == OrderStatus.CANCELLED;
    }

    public boolean isCompletion() {
        return newStatus == OrderStatus.COMPLETED;
    }

    private int getStatusLevel(OrderStatus status) {
        switch (status) {
            case CREATED: return 1;
            case ASSIGNED: return 2;
            case IN_PROGRESS: return 3;
            case PARTIALLY_COMPLETED: return 4;
            case COMPLETED: return 5;
            case ON_HOLD: return 2;
            case CANCELLED: return 0;
            case FAILED: return 0;
            default: return 0;
        }
    }

    public String getStatusChangeDescription() {
        if (oldStatus == null) {
            return "Zamówienie utworzone ze statusem: " + newStatus.getDisplayName();
        }
        return "Status zmieniony z " + oldStatus.getDisplayName() + " na " + newStatus.getDisplayName();
    }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public OrderStatus getOldStatus() { return oldStatus; }
    public void setOldStatus(OrderStatus oldStatus) { this.oldStatus = oldStatus; }

    public OrderStatus getNewStatus() { return newStatus; }
    public void setNewStatus(OrderStatus newStatus) { this.newStatus = newStatus; }

    public User getChangedBy() { return changedBy; }
    public void setChangedBy(User changedBy) { this.changedBy = changedBy; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Boolean getSystemGenerated() { return systemGenerated; }
    public void setSystemGenerated(Boolean systemGenerated) { this.systemGenerated = systemGenerated; }

    @Override
    public String toString() {
        return "OrderStatusHistory{" +
                "id=" + getId() +
                ", order=" + (order != null ? order.getOrderNumber() : "null") +
                ", oldStatus=" + oldStatus +
                ", newStatus=" + newStatus +
                ", changedAt=" + changedAt +
                ", changedBy=" + (changedBy != null ? changedBy.getUsername() : "null") +
                '}';
    }
}