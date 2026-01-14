package ru.yandex.practicum.mymarket.checkout.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.cart.dto.CartView;
import ru.yandex.practicum.mymarket.cart.service.CartService;
import ru.yandex.practicum.mymarket.orders.model.OrderEntity;
import ru.yandex.practicum.mymarket.orders.model.OrderItemEntity;
import ru.yandex.practicum.mymarket.orders.repo.OrderItemRepository;
import ru.yandex.practicum.mymarket.orders.repo.OrderRepository;
import ru.yandex.practicum.mymarket.payments.service.PaymentService;

import java.time.LocalDateTime;

@Service
public class CheckoutService {

    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final TransactionalOperator tx;
    private final PaymentService paymentService;

    public CheckoutService(
            CartService cartService,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            TransactionalOperator tx,
            PaymentService paymentService
    ) {
        this.cartService = cartService;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.tx = tx;
        this.paymentService = paymentService;
    }

    public Mono<Long> buy() {
        return cartService.getCartView()
                .flatMap(cart -> {
                    long total = cart.total();
                    return paymentService.pay(total)
                            .then(Mono.defer(() -> createOrderInTx(cart)));
                });
    }

    private Mono<Long> createOrderInTx(CartView cart) {
        Mono<Long> flow = Mono.defer(() -> {
            OrderEntity order = new OrderEntity();
            order.setCreatedAt(LocalDateTime.now());

            return orderRepository.save(order)
                    .flatMap(saved ->
                            Flux.fromIterable(cart.items())
                                    .filter(i -> i.count() > 0)
                                    .map(i -> {
                                        OrderItemEntity oi = new OrderItemEntity(
                                                i.id(),
                                                i.title(),
                                                i.price(),
                                                i.count()
                                        );
                                        oi.setOrderId(saved.getId());
                                        return oi;
                                    })
                                    .flatMap(orderItemRepository::save)
                                    .then(cartService.clear())
                                    .thenReturn(saved.getId())
                    );
        });

        return tx.transactional(flow);
    }
}
