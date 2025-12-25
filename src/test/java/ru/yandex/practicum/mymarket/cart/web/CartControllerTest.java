package ru.yandex.practicum.mymarket.cart.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.cart.model.CartItemEntity;
import ru.yandex.practicum.mymarket.cart.repo.CartItemRepository;
import ru.yandex.practicum.mymarket.items.model.ItemEntity;
import ru.yandex.practicum.mymarket.items.repo.ItemRepository;
import ru.yandex.practicum.mymarket.orders.repo.OrderRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class CartControllerTest {

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
        Mono.when(
                orderRepository.deleteAll(),
                cartItemRepository.deleteAll(),
                itemRepository.deleteAll()
        ).block();
    }

    @Test
    void getCart_returnsHtmlWithTotal() {
        ItemEntity item = itemRepository.save(new ItemEntity("A", "a", "/images/a.jpg", 100L)).block();
        cartItemRepository.save(new CartItemEntity(item.getId(), 2)).block();

        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(body -> {
                    assertThat(body).contains("A");
                    assertThat(body).contains("Итого");
                    assertThat(body).contains("200");
                });
    }

    @Test
    void postCart_deleteRemovesRow() {
        itemRepository.save(new ItemEntity("A", "a", "/images/a.jpg", 100L)).block();
        Long itemId = itemRepository.findAll().next().map(ItemEntity::getId).block();

        cartItemRepository.save(new CartItemEntity(itemId, 2)).block();

        webTestClient.post()
                .uri("/cart/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("id", String.valueOf(itemId))
                        .with("action", "DELETE")
                )
                .exchange()
                .expectStatus().isOk();

        StepVerifier.create(cartItemRepository.findByItemId(itemId))
                .verifyComplete();
    }
}
