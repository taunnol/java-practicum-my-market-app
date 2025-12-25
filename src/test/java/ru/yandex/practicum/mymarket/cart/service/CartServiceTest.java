package ru.yandex.practicum.mymarket.cart.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.mymarket.cart.model.CartItemEntity;
import ru.yandex.practicum.mymarket.cart.repo.CartItemRepository;
import ru.yandex.practicum.mymarket.common.dto.CartAction;
import ru.yandex.practicum.mymarket.items.model.ItemEntity;
import ru.yandex.practicum.mymarket.items.repo.ItemRepository;
import ru.yandex.practicum.mymarket.testutil.TestEntityIds;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private CartService cartService;

    @Captor
    private ArgumentCaptor<CartItemEntity> entityCaptor;

    @Test
    void plus_createsNewRow_whenAbsent() {
        when(cartItemRepository.findByItemId(10L)).thenReturn(Optional.empty());

        cartService.changeCount(10L, CartAction.PLUS);

        verify(cartItemRepository).save(entityCaptor.capture());
        CartItemEntity saved = entityCaptor.getValue();
        assertThat(saved.getItemId()).isEqualTo(10L);
        assertThat(saved.getCount()).isEqualTo(1);

        verify(cartItemRepository, never()).delete(any(CartItemEntity.class));
    }

    @Test
    void plus_increments_whenExists() {
        CartItemEntity existing = new CartItemEntity(10L, 2);
        when(cartItemRepository.findByItemId(10L)).thenReturn(Optional.of(existing));

        cartService.changeCount(10L, CartAction.PLUS);

        verify(cartItemRepository).save(entityCaptor.capture());
        CartItemEntity saved = entityCaptor.getValue();
        assertThat(saved.getItemId()).isEqualTo(10L);
        assertThat(saved.getCount()).isEqualTo(3);
    }

    @Test
    void minus_decrements_whenCountMoreThanOne() {
        CartItemEntity existing = new CartItemEntity(10L, 2);
        when(cartItemRepository.findByItemId(10L)).thenReturn(Optional.of(existing));

        cartService.changeCount(10L, CartAction.MINUS);

        verify(cartItemRepository).save(entityCaptor.capture());
        CartItemEntity saved = entityCaptor.getValue();
        assertThat(saved.getCount()).isEqualTo(1);
        verify(cartItemRepository, never()).delete(any(CartItemEntity.class));
    }

    @Test
    void minus_deletes_whenCountBecomesZero() {
        CartItemEntity existing = new CartItemEntity(10L, 1);
        when(cartItemRepository.findByItemId(10L)).thenReturn(Optional.of(existing));

        cartService.changeCount(10L, CartAction.MINUS);

        verify(cartItemRepository).delete(existing);
        verify(cartItemRepository, never()).save(any(CartItemEntity.class));
    }

    @Test
    void delete_removesRow_whenExists() {
        CartItemEntity existing = new CartItemEntity(10L, 5);
        when(cartItemRepository.findByItemId(10L)).thenReturn(Optional.of(existing));

        cartService.changeCount(10L, CartAction.DELETE);

        verify(cartItemRepository).delete(existing);
        verify(cartItemRepository, never()).save(any(CartItemEntity.class));
    }

    @Test
    void minus_doesNothing_whenAbsent() {
        when(cartItemRepository.findByItemId(10L)).thenReturn(Optional.empty());

        cartService.changeCount(10L, CartAction.MINUS);

        verify(cartItemRepository, never()).save(any());
        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    void getCountsByItemId_returnsMap() {
        when(cartItemRepository.findAll()).thenReturn(List.of(
                new CartItemEntity(1L, 2),
                new CartItemEntity(2L, 1)
        ));

        Map<Long, Integer> map = cartService.getCountsByItemId();

        assertThat(map).containsEntry(1L, 2);
        assertThat(map).containsEntry(2L, 1);
    }

    @Test
    void getCartView_buildsItemsAndTotal() {
        when(cartItemRepository.findAll()).thenReturn(List.of(
                new CartItemEntity(10L, 2),
                new CartItemEntity(20L, 1)
        ));

        ItemEntity e1 = new ItemEntity("A", "d", "/images/a.jpg", 100L);
        ItemEntity e2 = new ItemEntity("B", "d", "/images/b.jpg", 50L);

        TestEntityIds.setId(e1, 10L);
        TestEntityIds.setId(e2, 20L);

        when(itemRepository.findAllById(any())).thenReturn(List.of(e1, e2));

        var view = cartService.getCartView();

        assertThat(view.items()).hasSize(2);
        assertThat(view.total()).isEqualTo(100L * 2 + 50L);
        assertThat(view.items().getFirst().imgPath()).isEqualTo("images/a.jpg");
    }
}
