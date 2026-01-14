package ru.yandex.practicum.mymarket.orders.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("order_items")
public class OrderItemEntity {

    @Id
    private Long id;

    @Column("order_id")
    private Long orderId;

    @Column("item_id")
    private Long itemId;

    private String title;

    private Long price;

    private Integer count;

    public OrderItemEntity() {
        // for Spring Data
    }

    public OrderItemEntity(Long itemId, String title, Long price, Integer count) {
        this.itemId = itemId;
        this.title = title;
        this.price = price;
        this.count = count;
    }

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getItemId() {
        return itemId;
    }

    public String getTitle() {
        return title;
    }

    public Long getPrice() {
        return price;
    }

    public Integer getCount() {
        return count;
    }
}
