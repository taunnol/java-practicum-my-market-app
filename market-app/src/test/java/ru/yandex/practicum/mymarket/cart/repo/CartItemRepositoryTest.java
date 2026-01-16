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
import ru.yandex.practicum.mymarket.testutil.TestAuth;
import ru.yandex.practicum.mymarket.users.repo.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;

@MyMarketDataR2dbcTest
class CartItemRepositoryTest {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanup() {
        StepVerifier.create(Mono.when(
                cartItemRepository.deleteAll(),
                itemRepository.deleteAll()
        )).verifyComplete();
    }

    @Test
    void findByUserIdAndItemId_returnsEntity_whenExists() {
        long userId = TestAuth.userIdBlocking(userRepository, "user");

        long itemId = itemRepository.save(new ItemEntity("X", "d", "/img", 10L))
                .map(ItemEntity::getId)
                .block();

        cartItemRepository.save(new CartItemEntity(userId, itemId, 2)).block();

        StepVerifier.create(cartItemRepository.findByUserIdAndItemId(userId, itemId))
                .assertNext(found -> {
                    assertThat(found.getUserId()).isEqualTo(userId);
                    assertThat(found.getItemId()).isEqualTo(itemId);
                    assertThat(found.getCount()).isEqualTo(2);
                })
                .verifyComplete();
    }

    @Test
    void deleteByUserIdAndItemId_removesRow() {
        long userId = TestAuth.userIdBlocking(userRepository, "user");

        long itemId = itemRepository.save(new ItemEntity("X", "d", "/img", 10L))
                .map(ItemEntity::getId)
                .block();

        cartItemRepository.save(new CartItemEntity(userId, itemId, 2)).block();

        StepVerifier.create(cartItemRepository.deleteByUserIdAndItemId(userId, itemId))
                .expectNext(1L)
                .verifyComplete();

        StepVerifier.create(cartItemRepository.findByUserIdAndItemId(userId, itemId))
                .verifyComplete();
    }

    @Test
    void uniqueConstraint_preventsTwoRowsWithSameUserIdAndItemId_butAllowsDifferentUsers() {
        long userId = TestAuth.userIdBlocking(userRepository, "user");
        long user2Id = TestAuth.userIdBlocking(userRepository, "user2");

        long itemId = itemRepository.save(new ItemEntity("X", "d", "/img", 10L))
                .map(ItemEntity::getId)
                .block();

        cartItemRepository.save(new CartItemEntity(userId, itemId, 1)).block();

        StepVerifier.create(cartItemRepository.save(new CartItemEntity(userId, itemId, 2)))
                .expectErrorSatisfies(ex -> assertThat(ex).isInstanceOf(DataIntegrityViolationException.class))
                .verify();

        StepVerifier.create(cartItemRepository.save(new CartItemEntity(user2Id, itemId, 2)))
                .assertNext(saved -> {
                    assertThat(saved.getUserId()).isEqualTo(user2Id);
                    assertThat(saved.getItemId()).isEqualTo(itemId);
                    assertThat(saved.getCount()).isEqualTo(2);
                })
                .verifyComplete();
    }
}
