package ru.yandex.practicum.mymarket.cart.model;

import jakarta.persistence.*;

@Entity
@Table(
        name = "cart_items",
        uniqueConstraints = @UniqueConstraint(name = "uk_cart_items_item_id", columnNames = "item_id")
)
public class CartItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(nullable = false)
    private Integer count;

    protected CartItemEntity() {
        // for JPA
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
