package ru.yandex.practicum.mymarket.items.web;

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
import ru.yandex.practicum.mymarket.testsupport.MyMarketSpringBootTest;
import ru.yandex.practicum.mymarket.testsupport.RedisSpringBootTestBase;

import static org.assertj.core.api.Assertions.assertThat;

@MyMarketSpringBootTest
class ItemsControllerTest extends RedisSpringBootTestBase {

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
    void getItems_returnsHtml() {
        itemRepository.saveAll(java.util.List.of(
                new ItemEntity("t1", "d1", "/images/1.jpg", 10L),
                new ItemEntity("t2", "d2", "/images/2.jpg", 20L)
        )).collectList().block();

        webTestClient.get()
                .uri("/items?search=&sort=NO&pageNumber=1&pageSize=5")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(body -> {
                    assertThat(body).contains("Витрина магазина");
                    assertThat(body).contains("t1");
                    assertThat(body).contains("t2");
                });
    }

    @Test
    void postItems_changesCart_andRedirectsKeepingQueryParams() {
        itemRepository.save(new ItemEntity("t1", "d1", "/images/1.jpg", 10L)).block();
        Long itemId = itemRepository.findAll().next().map(ItemEntity::getId).block();

        webTestClient.post()
                .uri("/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("id", String.valueOf(itemId))
                        .with("action", "PLUS")
                        .with("search", "q")
                        .with("sort", "PRICE")
                        .with("pageNumber", "2")
                        .with("pageSize", "20")
                )
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().value("Location", loc -> {
                    assertThat(loc).contains("/items");
                    assertThat(loc).contains("search=q");
                    assertThat(loc).contains("sort=PRICE");
                    assertThat(loc).contains("pageNumber=2");
                    assertThat(loc).contains("pageSize=20");
                });

        Integer count = cartItemRepository.findByItemId(itemId).map(CartItemEntity::getCount).block();
        assertThat(count).isEqualTo(1);
    }

    @Test
    void getItems_invalidSort_returns400() {
        webTestClient.get()
                .uri("/items?sort=BAD")
                .exchange()
                .expectStatus().isBadRequest();
    }
}
