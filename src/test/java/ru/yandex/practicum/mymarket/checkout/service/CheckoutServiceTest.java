package ru.yandex.practicum.mymarket.checkout.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.mymarket.cart.dto.CartView;
import ru.yandex.practicum.mymarket.cart.service.CartService;
import ru.yandex.practicum.mymarket.items.dto.ItemDto;
import ru.yandex.practicum.mymarket.orders.model.OrderEntity;
import ru.yandex.practicum.mymarket.orders.repo.OrderRepository;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock
    private CartService cartService;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private CheckoutService checkoutService;

    @Captor
    private ArgumentCaptor<OrderEntity> orderCaptor;

    private static void setId(OrderEntity entity, long id) {
        try {
            Field f = OrderEntity.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void buy_createsOrderFromCart_andClearsCart() {
        when(cartService.getCartView()).thenReturn(new CartView(
                List.of(
                        new ItemDto(10, "A", "d", "images/a.jpg", 100, 2),
                        new ItemDto(20, "B", "d", "images/b.jpg", 50, 1)
                ),
                250L
        ));

        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> {
            OrderEntity o = inv.getArgument(0);
            setId(o, 123L);
            return o;
        });

        long id = checkoutService.buy();

        assertThat(id).isEqualTo(123L);

        verify(orderRepository).save(orderCaptor.capture());
        OrderEntity saved = orderCaptor.getValue();
        assertThat(saved.getItems()).hasSize(2);
        assertThat(saved.getItems().getFirst().getTitle()).isEqualTo("A");
        assertThat(saved.getItems().getFirst().getPrice()).isEqualTo(100L);
        assertThat(saved.getItems().getFirst().getCount()).isEqualTo(2);

        verify(cartService).clear();
    }
}
