package ru.yandex.practicum.mymarket.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.cart.repo.CartItemRepository;
import ru.yandex.practicum.mymarket.items.model.ItemEntity;
import ru.yandex.practicum.mymarket.items.repo.ItemRepository;
import ru.yandex.practicum.mymarket.orders.repo.OrderRepository;
import ru.yandex.practicum.mymarket.testsupport.MyMarketSpringBootTest;
import ru.yandex.practicum.mymarket.testsupport.RedisSpringBootTestBase;

import static org.assertj.core.api.Assertions.assertThat;

@MyMarketSpringBootTest
class AnonymousUISecurityIT extends RedisSpringBootTestBase {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void cleanup() {
        StepVerifier.create(Mono.when(
                orderRepository.deleteAll(),
                cartItemRepository.deleteAll(),
                itemRepository.deleteAll()
        )).verifyComplete();
    }

    @Test
    void anonymous_itemsPage_hasNoPrivateLinksAndNoCartControls() {
        String title = "ANON_ITEM_123";

        StepVerifier.create(itemRepository.save(new ItemEntity(title, "d", "/images/a.jpg", 10L)).then())
                .verifyComplete();

        webTestClient.get()
                .uri("/items?search=&sort=NO&pageNumber=1&pageSize=5")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(html -> {
                    assertThat(html).contains("Витрина магазина");
                    assertThat(html).contains(title);

                    assertThat(html).doesNotContain("href=\"/orders\"");
                    assertThat(html).doesNotContain("href=\"/cart/items\"");

                    assertThat(html).doesNotContain("value=\"PLUS\"");
                    assertThat(html).doesNotContain("value=\"MINUS\"");
                    assertThat(html).doesNotContain("value=\"DELETE\"");
                });
    }

    @Test
    void anonymous_privatePages_redirectToLogin() {
        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().value("Location", loc -> assertThat(loc).contains("/login"));

        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().value("Location", loc -> assertThat(loc).contains("/login"));
    }
}
