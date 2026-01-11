package ru.yandex.practicum.mymarket.cart.repo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.cart.model.CartItemEntity;
import ru.yandex.practicum.mymarket.items.model.ItemEntity;
import ru.yandex.practicum.mymarket.items.repo.ItemRepository;
import ru.yandex.practicum.mymarket.testsupport.MyMarketDataR2dbcTest;

import static org.assertj.core.api.Assertions.assertThat;

@MyMarketDataR2dbcTest
class CartItemRepositoryTest {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    void cleanup() {
        StepVerifier.create(Mono.when(
                cartItemRepository.deleteAll(),
                itemRepository.deleteAll()
        )).verifyComplete();
    }

    @Test
    void findByItemId_returnsEntity_whenExists() {
        long itemId = itemRepository.save(new ItemEntity("X", "d", "/img", 10L))
                .map(ItemEntity::getId)
                .block();

        cartItemRepository.save(new CartItemEntity(itemId, 2)).block();

        StepVerifier.create(cartItemRepository.findByItemId(itemId))
                .assertNext(found -> {
                    assertThat(found.getItemId()).isEqualTo(itemId);
                    assertThat(found.getCount()).isEqualTo(2);
                })
                .verifyComplete();
    }

    @Test
    void deleteByItemId_removesRow() {
        long itemId = itemRepository.save(new ItemEntity("X", "d", "/img", 10L))
                .map(ItemEntity::getId)
                .block();

        cartItemRepository.save(new CartItemEntity(itemId, 2)).block();

        StepVerifier.create(cartItemRepository.deleteByItemId(itemId))
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier.create(cartItemRepository.findByItemId(itemId))
                .verifyComplete();
    }

    @Test
    void uniqueConstraint_preventsTwoRowsWithSameItemId() {
        long itemId = itemRepository.save(new ItemEntity("X", "d", "/img", 10L))
                .map(ItemEntity::getId)
                .block();

        cartItemRepository.save(new CartItemEntity(itemId, 1)).block();

        StepVerifier.create(cartItemRepository.save(new CartItemEntity(itemId, 2)))
                .expectErrorSatisfies(ex -> assertThat(ex).isInstanceOf(DataIntegrityViolationException.class))
                .verify();
    }
}
