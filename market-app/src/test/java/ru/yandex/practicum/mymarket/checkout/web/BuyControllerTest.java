package ru.yandex.practicum.mymarket.checkout.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.cart.model.CartItemEntity;
import ru.yandex.practicum.mymarket.cart.repo.CartItemRepository;
import ru.yandex.practicum.mymarket.items.model.ItemEntity;
import ru.yandex.practicum.mymarket.items.repo.ItemRepository;
import ru.yandex.practicum.mymarket.orders.model.OrderEntity;
import ru.yandex.practicum.mymarket.orders.repo.OrderRepository;
import ru.yandex.practicum.mymarket.payments.service.PaymentService;
import ru.yandex.practicum.mymarket.testsupport.MyMarketSpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@MyMarketSpringBootTest
class BuyControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @MockBean
    private PaymentService paymentService;

    @BeforeEach
    void cleanup() {
        when(paymentService.pay(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(Mono.when(
                orderRepository.deleteAll(),
                cartItemRepository.deleteAll(),
                itemRepository.deleteAll()
        )).verifyComplete();
    }

    @Test
    void buy_redirectsToNewOrderPage_andCreatesOrder_whenPaymentsOk() {
        StepVerifier.create(
                        itemRepository.save(new ItemEntity("A", "a", "/images/a.jpg", 1200L))
                                .flatMap(item -> cartItemRepository.save(new CartItemEntity(item.getId(), 1)).then())
                )
                .verifyComplete();

        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().value("Location", loc -> {
                    assertThat(loc).contains("/orders/");
                    assertThat(loc).contains("newOrder=true");
                });

        OrderEntity order = orderRepository.findAll().next().block();
        assertThat(order).isNotNull();
    }
}
