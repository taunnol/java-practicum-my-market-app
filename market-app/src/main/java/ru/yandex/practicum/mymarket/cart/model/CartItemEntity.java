package ru.yandex.practicum.mymarket.cart.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("cart_items")
public class CartItemEntity {

    @Id
    private Long id;

    @Column("user_id")
    private Long userId;

    @Column("item_id")
    private Long itemId;

    private Integer count;

    public CartItemEntity() {
    }

    public CartItemEntity(Long userId, Long itemId, Integer count) {
        this.userId = userId;
        this.itemId = itemId;
        this.count = count;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
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
