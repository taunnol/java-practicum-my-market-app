package ru.yandex.practicum.mymarket.orders.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.orders.model.OrderEntity;
import ru.yandex.practicum.mymarket.orders.model.OrderItemEntity;
import ru.yandex.practicum.mymarket.orders.repo.OrderItemRepository;
import ru.yandex.practicum.mymarket.orders.repo.OrderRepository;
import ru.yandex.practicum.mymarket.testutil.TestEntityIds;
import ru.yandex.practicum.mymarket.users.security.CurrentUserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void getOrder_calculatesTotalSum() {
        when(currentUserService.currentUserId()).thenReturn(Mono.just(42L));

        OrderEntity o = new OrderEntity();
        TestEntityIds.setId(o, 10L);

        when(orderRepository.findByIdAndUserId(10L, 42L)).thenReturn(Mono.just(o));
        when(orderItemRepository.findAllByOrderId(10L)).thenReturn(Flux.just(
                new OrderItemEntity(1L, "A", 100L, 2),
                new OrderItemEntity(2L, "B", 50L, 1)
        ));

        StepVerifier.create(orderService.getOrder(10L))
                .assertNext(dto -> {
                    assertThat(dto.id()).isEqualTo(10L);
                    assertThat(dto.items()).hasSize(2);
                    assertThat(dto.totalSum()).isEqualTo(250L);
                })
                .verifyComplete();
    }

    @Test
    void getOrders_returnsList() {
        when(currentUserService.currentUserId()).thenReturn(Mono.just(42L));

        OrderEntity o1 = new OrderEntity();
        TestEntityIds.setId(o1, 1L);
        OrderEntity o2 = new OrderEntity();
        TestEntityIds.setId(o2, 2L);

        when(orderRepository.findAllByUserIdOrderByIdDesc(42L)).thenReturn(Flux.just(o2, o1));
        when(orderItemRepository.findAllByOrderId(2L)).thenReturn(Flux.empty());
        when(orderItemRepository.findAllByOrderId(1L)).thenReturn(Flux.empty());

        StepVerifier.create(orderService.getOrders())
                .assertNext(list -> assertThat(list).hasSize(2))
                .verifyComplete();
    }
}
