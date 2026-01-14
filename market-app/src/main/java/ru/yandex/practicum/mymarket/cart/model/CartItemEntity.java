package ru.yandex.practicum.mymarket.cart.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("cart_items")
public class CartItemEntity {

    @Id
    private Long id;

    @Column("item_id")
    private Long itemId;

    private Integer count;

    public CartItemEntity() {
        // for Spring Data
    }

    public CartItemEntity(Long itemId, Integer count) {
        this.itemId = itemId;
        this.count = count;
    }

    public Long getId() {
        return id;
    }

    public Long getItemId() {
        return itemId;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
