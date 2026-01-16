package ru.yandex.practicum.mymarket.checkout.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import ru.yandex.practicum.mymarket.users.security.CurrentUserService;

import java.time.LocalDateTime;

@Service
public class CheckoutService {

    private static final Logger log = LoggerFactory.getLogger(CheckoutService.class);

    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final TransactionalOperator tx;
    private final PaymentService paymentService;
    private final CurrentUserService currentUserService;

    public CheckoutService(
            CartService cartService,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            TransactionalOperator tx,
            PaymentService paymentService,
            CurrentUserService currentUserService
    ) {
        this.cartService = cartService;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.tx = tx;
        this.paymentService = paymentService;
        this.currentUserService = currentUserService;
    }

    public Mono<Long> buy() {
        return currentUserService.currentUserId()
                .flatMap(userId -> cartService.getCartView()
                        .flatMap(cart -> {
                            long total = cart.total();
                            if (total <= 0) {
                                return Mono.error(new IllegalStateException("Cart is empty"));
                            }

                            return createOrderDraftInTx(userId, cart)
                                    .flatMap(orderId ->
                                            paymentService.pay(total)
                                                    .onErrorResume(payError ->
                                                            deleteDraftOrder(orderId, payError)
                                                                    .then(Mono.error(payError))
                                                    )
                                                    .then(finalizeAfterPayment(orderId))
                                                    .thenReturn(orderId)
                                    );
                        })
                );
    }

    private Mono<Long> createOrderDraftInTx(long userId, CartView cart) {
        Mono<Long> flow = Mono.defer(() -> {
            OrderEntity order = new OrderEntity();
            order.setUserId(userId);
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
                                    .then(Mono.just(saved.getId()))
                    );
        });

        return tx.transactional(flow);
    }

    private Mono<Void> finalizeAfterPayment(long orderId) {
        return Mono.defer(() -> tx.transactional(cartService.clear()))
                .onErrorResume(e -> {
                    log.error("Failed to clear cart after successful payment, orderId={}", orderId, e);
                    return Mono.empty();
                });
    }

    private Mono<Void> deleteDraftOrder(long orderId, Throwable payError) {
        return orderRepository.deleteById(orderId)
                .onErrorResume(deleteError -> {
                    log.error("Failed to delete draft order after payment error, orderId={}, payError={}",
                            orderId, payError.toString(), deleteError);
                    return Mono.empty();
                });
    }
}
