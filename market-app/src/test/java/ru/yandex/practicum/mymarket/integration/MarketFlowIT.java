package ru.yandex.practicum.mymarket.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.cart.repo.CartItemRepository;
import ru.yandex.practicum.mymarket.items.model.ItemEntity;
import ru.yandex.practicum.mymarket.items.repo.ItemRepository;
import ru.yandex.practicum.mymarket.orders.model.OrderEntity;
import ru.yandex.practicum.mymarket.orders.repo.OrderRepository;
import ru.yandex.practicum.mymarket.payments.service.BuyAvailability;
import ru.yandex.practicum.mymarket.payments.service.BuyAvailabilityService;
import ru.yandex.practicum.mymarket.payments.service.PaymentService;
import ru.yandex.practicum.mymarket.testsupport.MyMarketSpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@MyMarketSpringBootTest
class MarketFlowIT {

    private final WebTestClient webTestClient;
    private final ItemRepository itemRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;

    @MockBean
    private BuyAvailabilityService buyAvailabilityService;

    @MockBean
    private PaymentService paymentService;

    MarketFlowIT(WebTestClient webTestClient,
                 ItemRepository itemRepository,
                 CartItemRepository cartItemRepository,
                 OrderRepository orderRepository) {
        this.webTestClient = webTestClient;
        this.itemRepository = itemRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
    }

    @BeforeEach
    void cleanup() {
        when(buyAvailabilityService.getBuyAvailability(anyLong())).thenReturn(Mono.just(BuyAvailability.ok()));
        when(paymentService.pay(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(Mono.when(
                orderRepository.deleteAll(),
                cartItemRepository.deleteAll(),
                itemRepository.deleteAll()
        )).verifyComplete();
    }

    @Test
    void flow_itemsToCartToBuyToOrder() {
        ItemEntity ball = itemRepository.save(new ItemEntity(
                "Abobs",
                "aboba",
                "/images/aboba.jpg",
                1200L
        )).block();

        long itemId = ball.getId();

        webTestClient.get()
                .uri("/items")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML);

        webTestClient.post()
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

        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    assertThat(body).contains("Abobs");
                    assertThat(body).contains("1200");
                });

        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection();

        OrderEntity order = orderRepository.findAll().next().block();
        assertThat(order).isNotNull();

        webTestClient.get()
                .uri("/orders/" + order.getId() + "?newOrder=true")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("Поздравляем"));
    }
}
