package ru.yandex.practicum.mymarket.orders.model;

import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private Integer count;

    protected OrderItemEntity() {
        // for JPA
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

    public OrderEntity getOrder() {
        return order;
    }

    void setOrder(OrderEntity order) {
        this.order = order;
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
