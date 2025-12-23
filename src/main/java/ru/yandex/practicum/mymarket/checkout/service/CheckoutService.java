package ru.yandex.practicum.mymarket.checkout.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mymarket.cart.dto.CartView;
import ru.yandex.practicum.mymarket.cart.service.CartService;
import ru.yandex.practicum.mymarket.items.dto.ItemDto;
import ru.yandex.practicum.mymarket.orders.model.OrderEntity;
import ru.yandex.practicum.mymarket.orders.model.OrderItemEntity;
import ru.yandex.practicum.mymarket.orders.repo.OrderRepository;

@Service
public class CheckoutService {

    private final CartService cartService;
    private final OrderRepository orderRepository;

    public CheckoutService(CartService cartService, OrderRepository orderRepository) {
        this.cartService = cartService;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public long buy() {
        CartView cart = cartService.getCartView();

        OrderEntity order = new OrderEntity();
        for (ItemDto item : cart.items()) {
            if (item.count() <= 0) {
                continue;
            }
            order.addItem(new OrderItemEntity(
                    item.id(),
                    item.title(),
                    item.price(),
                    item.count()
            ));
        }

        OrderEntity saved = orderRepository.save(order);
        cartService.clear();
        return saved.getId();
    }
}
