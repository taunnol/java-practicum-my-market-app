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

    @Id
    private Long id;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Transient
    private final List<OrderItemEntity> items = new ArrayList<>();

    public OrderEntity() {
        // for Spring Data
    }

    public Long getId() {
        return id;
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
