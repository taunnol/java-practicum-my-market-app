package ru.yandex.practicum.mymarket.cart.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.cart.model.CartItemEntity;
import ru.yandex.practicum.mymarket.cart.repo.CartItemRepository;
import ru.yandex.practicum.mymarket.common.dto.CartAction;
import ru.yandex.practicum.mymarket.items.cache.CachedItem;
import ru.yandex.practicum.mymarket.items.cache.CachedItemService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private CachedItemService cachedItemService;

    @InjectMocks
    private CartService cartService;

    @Captor
    private ArgumentCaptor<CartItemEntity> entityCaptor;

    @Test
    void plus_createsNewRow_whenAbsent() {
        when(cartItemRepository.findByItemId(10L)).thenReturn(Mono.empty());
        when(cartItemRepository.save(any(CartItemEntity.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(cartService.changeCount(10L, CartAction.PLUS))
                .verifyComplete();

        verify(cartItemRepository).save(entityCaptor.capture());
        CartItemEntity saved = entityCaptor.getValue();
        assertThat(saved.getItemId()).isEqualTo(10L);
        assertThat(saved.getCount()).isEqualTo(1);

        verify(cartItemRepository, never()).delete(any(CartItemEntity.class));
    }

    @Test
    void plus_increments_whenExists() {
        CartItemEntity existing = new CartItemEntity(10L, 2);

        when(cartItemRepository.findByItemId(10L)).thenReturn(Mono.just(existing));
        when(cartItemRepository.save(any(CartItemEntity.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(cartService.changeCount(10L, CartAction.PLUS))
                .verifyComplete();

        verify(cartItemRepository).save(entityCaptor.capture());
        assertThat(entityCaptor.getValue().getCount()).isEqualTo(3);
    }

    @Test
    void minus_decrements_whenCountMoreThanOne() {
        CartItemEntity existing = new CartItemEntity(10L, 2);

        when(cartItemRepository.findByItemId(10L)).thenReturn(Mono.just(existing));
        when(cartItemRepository.save(any(CartItemEntity.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(cartService.changeCount(10L, CartAction.MINUS))
                .verifyComplete();

        verify(cartItemRepository).save(entityCaptor.capture());
        assertThat(entityCaptor.getValue().getCount()).isEqualTo(1);
        verify(cartItemRepository, never()).delete(any(CartItemEntity.class));
    }

    @Test
    void minus_deletes_whenCountBecomesZero() {
        CartItemEntity existing = new CartItemEntity(10L, 1);

        when(cartItemRepository.findByItemId(10L)).thenReturn(Mono.just(existing));
        when(cartItemRepository.delete(existing)).thenReturn(Mono.empty());

        StepVerifier.create(cartService.changeCount(10L, CartAction.MINUS))
                .verifyComplete();

        verify(cartItemRepository).delete(existing);
        verify(cartItemRepository, never()).save(any(CartItemEntity.class));
    }

    @Test
    void delete_removesRow_whenExists() {
        CartItemEntity existing = new CartItemEntity(10L, 5);

        when(cartItemRepository.findByItemId(10L)).thenReturn(Mono.just(existing));
        when(cartItemRepository.delete(existing)).thenReturn(Mono.empty());

        StepVerifier.create(cartService.changeCount(10L, CartAction.DELETE))
                .verifyComplete();

        verify(cartItemRepository).delete(existing);
        verify(cartItemRepository, never()).save(any(CartItemEntity.class));
    }

    @Test
    void minus_doesNothing_whenAbsent() {
        when(cartItemRepository.findByItemId(10L)).thenReturn(Mono.empty());

        StepVerifier.create(cartService.changeCount(10L, CartAction.MINUS))
                .verifyComplete();

        verify(cartItemRepository, never()).save(any());
        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    void getCountsByItemId_returnsMap() {
        when(cartItemRepository.findAll()).thenReturn(Flux.just(
                new CartItemEntity(1L, 2),
                new CartItemEntity(2L, 1)
        ));

        StepVerifier.create(cartService.getCountsByItemId())
                .assertNext(map -> {
                    assertThat(map).containsEntry(1L, 2);
                    assertThat(map).containsEntry(2L, 1);
                })
                .verifyComplete();
    }

    @Test
    void getCartView_buildsItemsAndTotal_andNormalizesImgPath() {
        when(cartItemRepository.findAll()).thenReturn(Flux.just(
                new CartItemEntity(10L, 2),
                new CartItemEntity(20L, 1)
        ));

        CachedItem i1 = new CachedItem(10L, "A", "d", "/images/a.jpg", 100L);
        CachedItem i2 = new CachedItem(20L, "B", "d", "/images/b.jpg", 50L);

        when(cachedItemService.getItems(anyCollection())).thenReturn(Flux.just(i1, i2));

        StepVerifier.create(cartService.getCartView())
                .assertNext(view -> {
                    assertThat(view.items()).hasSize(2);
                    assertThat(view.total()).isEqualTo(100L * 2 + 50L);
                    assertThat(view.items().getFirst().imgPath()).isEqualTo("images/a.jpg");
                })
                .verifyComplete();
    }
}
