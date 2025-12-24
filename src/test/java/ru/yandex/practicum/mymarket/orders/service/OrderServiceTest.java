package ru.yandex.practicum.mymarket.orders.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.mymarket.orders.model.OrderEntity;
import ru.yandex.practicum.mymarket.orders.model.OrderItemEntity;
import ru.yandex.practicum.mymarket.orders.repo.OrderRepository;
import ru.yandex.practicum.mymarket.testutil.TestEntityIds;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void getOrder_calculatesTotalSum() {
        OrderEntity o = new OrderEntity();
        TestEntityIds.setId(o, 10L);
        o.addItem(new OrderItemEntity(1L, "A", 100L, 2));
        o.addItem(new OrderItemEntity(2L, "B", 50L, 1));

        when(orderRepository.findById(10L)).thenReturn(Optional.of(o));

        var dto = orderService.getOrder(10L);

        assertThat(dto.id()).isEqualTo(10L);
        assertThat(dto.items()).hasSize(2);
        assertThat(dto.totalSum()).isEqualTo(250L);
    }

    @Test
    void getOrders_returnsList() {
        OrderEntity o1 = new OrderEntity();
        TestEntityIds.setId(o1, 1L);
        OrderEntity o2 = new OrderEntity();
        TestEntityIds.setId(o2, 2L);

        when(orderRepository.findAll()).thenReturn(List.of(o1, o2));

        var list = orderService.getOrders();

        assertThat(list).hasSize(2);
    }
}
