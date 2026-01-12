package ru.yandex.practicum.mymarket.checkout.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.cart.dto.CartView;
import ru.yandex.practicum.mymarket.cart.service.CartService;
import ru.yandex.practicum.mymarket.items.dto.ItemDto;
import ru.yandex.practicum.mymarket.orders.model.OrderEntity;
import ru.yandex.practicum.mymarket.orders.model.OrderItemEntity;
import ru.yandex.practicum.mymarket.orders.repo.OrderItemRepository;
import ru.yandex.practicum.mymarket.orders.repo.OrderRepository;
import ru.yandex.practicum.mymarket.payments.service.InsufficientFundsException;
import ru.yandex.practicum.mymarket.payments.service.PaymentsClient;
import ru.yandex.practicum.mymarket.testutil.TestEntityIds;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock
    private CartService cartService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private TransactionalOperator tx;

    @Mock
    private PaymentsClient paymentsClient;

    @InjectMocks
    private CheckoutService checkoutService;

    @Captor
    private ArgumentCaptor<OrderItemEntity> orderItemCaptor;

    @Test
    void buy_createsOrderItems_andClearsCart_whenPaymentOk() {
        when(cartService.getCartView()).thenReturn(Mono.just(new CartView(
                List.of(
                        new ItemDto(10, "A", "d", "images/a.jpg", 100, 2),
                        new ItemDto(20, "B", "d", "images/b.jpg", 50, 1)
                ),
                250L
        )));

        when(paymentsClient.pay(250L)).thenReturn(Mono.empty());
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> {
            OrderEntity o = inv.getArgument(0);
            TestEntityIds.setId(o, 123L);
            return Mono.just(o);
        });

        when(orderItemRepository.save(any(OrderItemEntity.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(cartService.clear()).thenReturn(Mono.empty());

        when(tx.transactional(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));

        StepVerifier.create(checkoutService.buy())
                .expectNext(123L)
                .verifyComplete();

        verify(paymentsClient).pay(250L);

        verify(orderItemRepository, times(2)).save(orderItemCaptor.capture());
        List<OrderItemEntity> saved = orderItemCaptor.getAllValues();

        assertThat(saved).allMatch(i -> i.getOrderId() != null && i.getOrderId().equals(123L));

        verify(cartService).clear();
    }

    @Test
    void buy_doesNotCreateOrder_whenPaymentRejected() {
        when(cartService.getCartView()).thenReturn(Mono.just(new CartView(
                List.of(new ItemDto(10, "A", "d", "images/a.jpg", 100, 2)),
                200L
        )));

        when(paymentsClient.pay(200L)).thenReturn(Mono.error(new InsufficientFundsException("INSUFFICIENT_FUNDS")));

        StepVerifier.create(checkoutService.buy())
                .expectError(InsufficientFundsException.class)
                .verify();

        verify(paymentsClient).pay(200L);

        verify(orderRepository, never()).save(any());
        verify(orderItemRepository, never()).save(any());
        verify(cartService, never()).clear();
    }
}
