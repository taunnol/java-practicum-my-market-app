package ru.yandex.practicum.mymarket.orders.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Table("orders")
public class OrderEntity {

    @Transient
    private final List<OrderItemEntity> items = new ArrayList<>();

    @Id
    private Long id;

    @Column("user_id")
    private Long userId;

    @Column("created_at")
    private LocalDateTime createdAt;

    public OrderEntity() {
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<OrderItemEntity> getItems() {
        return items;
    }

    public void addItem(OrderItemEntity item) {
        items.add(item);
    }
}
