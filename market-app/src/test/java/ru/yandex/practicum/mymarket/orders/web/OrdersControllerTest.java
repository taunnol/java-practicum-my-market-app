package ru.yandex.practicum.mymarket.orders.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.cart.repo.CartItemRepository;
import ru.yandex.practicum.mymarket.items.repo.ItemRepository;
import ru.yandex.practicum.mymarket.orders.model.OrderEntity;
import ru.yandex.practicum.mymarket.orders.model.OrderItemEntity;
import ru.yandex.practicum.mymarket.orders.repo.OrderItemRepository;
import ru.yandex.practicum.mymarket.orders.repo.OrderRepository;
import ru.yandex.practicum.mymarket.testsupport.MyMarketSpringBootTest;
import ru.yandex.practicum.mymarket.testsupport.RedisSpringBootTestBase;
import ru.yandex.practicum.mymarket.testutil.TestAuth;
import ru.yandex.practicum.mymarket.users.repo.UserRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@MyMarketSpringBootTest
class OrdersControllerTest extends RedisSpringBootTestBase {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanup() {
        StepVerifier.create(Mono.when(
                orderRepository.deleteAll(),
                orderItemRepository.deleteAll(),
                cartItemRepository.deleteAll(),
                itemRepository.deleteAll()
        )).verifyComplete();
    }

    @Test
    void ordersPages_render() {
        long userId = TestAuth.userIdBlocking(userRepository, "user");

        OrderEntity order = new OrderEntity();
        order.setUserId(userId);
        order.setCreatedAt(LocalDateTime.now());

        OrderEntity saved = orderRepository.save(order).block();
        long id = saved.getId();

        OrderItemEntity oi = new OrderItemEntity(10L, "A", 100L, 2);
        oi.setOrderId(id);
        orderItemRepository.save(oi).block();

        WebTestClient auth = TestAuth.login(webTestClient, "user", "password");

        auth.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("Заказ №" + id));

        auth.get()
                .uri("/orders/" + id + "?newOrder=true")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("Поздравляем"));
    }

    @Test
    void user_cannotAccessOtherUsersOrder() {
        long user2Id = TestAuth.userIdBlocking(userRepository, "user2");

        OrderEntity order = new OrderEntity();
        order.setUserId(user2Id);
        order.setCreatedAt(LocalDateTime.now());

        long id = orderRepository.save(order).map(OrderEntity::getId).block();

        WebTestClient auth = TestAuth.login(webTestClient, "user", "password");

        auth.get()
                .uri("/orders/" + id)
                .exchange()
                .expectStatus().isNotFound();
    }
}
