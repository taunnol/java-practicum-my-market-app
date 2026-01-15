package ru.yandex.practicum.mymarket.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.cart.model.CartItemEntity;
import ru.yandex.practicum.mymarket.cart.repo.CartItemRepository;
import ru.yandex.practicum.mymarket.items.model.ItemEntity;
import ru.yandex.practicum.mymarket.items.repo.ItemRepository;
import ru.yandex.practicum.mymarket.orders.repo.OrderRepository;
import ru.yandex.practicum.mymarket.testutil.TestAuth;
import ru.yandex.practicum.mymarket.testsupport.MyMarketSpringBootTest;
import ru.yandex.practicum.mymarket.testsupport.RedisSpringBootTestBase;
import ru.yandex.practicum.mymarket.users.repo.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;

@MyMarketSpringBootTest
class CartIsolationIT extends RedisSpringBootTestBase {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanup() {
        StepVerifier.create(Mono.when(
                orderRepository.deleteAll(),
                cartItemRepository.deleteAll(),
                itemRepository.deleteAll()
        )).verifyComplete();
    }

    @Test
    void userCartIsNotVisibleToOtherUser() {
        String title = "USER1_ONLY_ITEM_123";

        ItemEntity item = itemRepository.save(new ItemEntity(title, "d", "/images/a.jpg", 10L)).block();
        Long itemId = item.getId();

        WebTestClient userClient = TestAuth.login(webTestClient, "user", "password");

        userClient.post()
                .uri("/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("id", String.valueOf(itemId))
                        .with("action", "PLUS")
                        .with("search", "")
                        .with("sort", "NO")
                        .with("pageNumber", "1")
                        .with("pageSize", "5"))
                .exchange()
                .expectStatus().is3xxRedirection();

        long userId = TestAuth.userIdBlocking(userRepository, "user");
        Integer userCount = cartItemRepository.findByUserIdAndItemId(userId, itemId)
                .map(CartItemEntity::getCount)
                .block();

        assertThat(userCount).isEqualTo(1);

        WebTestClient user2Client = TestAuth.login(webTestClient, "user2", "password2");

        user2Client.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> {
                    assertThat(html).contains("Корзина пуста.");
                    assertThat(html).doesNotContain(title);
                });

        long user2Id = TestAuth.userIdBlocking(userRepository, "user2");
        StepVerifier.create(cartItemRepository.findByUserIdAndItemId(user2Id, itemId))
                .verifyComplete();
    }
}
