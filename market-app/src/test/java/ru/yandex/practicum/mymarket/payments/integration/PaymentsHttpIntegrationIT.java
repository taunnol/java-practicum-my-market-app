package ru.yandex.practicum.mymarket.payments.integration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.cart.model.CartItemEntity;
import ru.yandex.practicum.mymarket.cart.repo.CartItemRepository;
import ru.yandex.practicum.mymarket.items.model.ItemEntity;
import ru.yandex.practicum.mymarket.items.repo.ItemRepository;
import ru.yandex.practicum.mymarket.orders.repo.OrderRepository;
import ru.yandex.practicum.mymarket.testsupport.MyMarketSpringBootTest;
import ru.yandex.practicum.mymarket.testsupport.RedisSpringBootTestBase;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@MyMarketSpringBootTest
class PaymentsHttpIntegrationIT extends RedisSpringBootTestBase {

    private static final PaymentsStubServer PAYMENTS = new PaymentsStubServer();
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private OrderRepository orderRepository;

    @DynamicPropertySource
    static void paymentsProps(DynamicPropertyRegistry registry) {
        PAYMENTS.startIfNeeded();
        registry.add("payments.base-url", PAYMENTS::baseUrl);
        registry.add("payments.timeout", () -> "200ms");
    }

    @AfterAll
    static void stop() {
        PAYMENTS.stop();
    }

    private static void assertBuyButtonDisabled(String html, boolean expectedDisabled) {
        int i = html.indexOf("id=\"buy-button\"");
        assertThat(i).isGreaterThan(0);

        int start = Math.max(0, i - 200);
        int end = Math.min(html.length(), i + 200);
        String snippet = html.substring(start, end);

        boolean hasDisabled = snippet.contains("disabled");
        if (expectedDisabled) {
            assertThat(hasDisabled)
                    .as("Buy button must be disabled. Snippet: %s", snippet)
                    .isTrue();
        } else {
            assertThat(hasDisabled)
                    .as("Buy button must be enabled. Snippet: %s", snippet)
                    .isFalse();
        }
    }

    @BeforeEach
    void cleanup() {
        PAYMENTS.setAvailable(true);
        PAYMENTS.resetBalance(10_000);

        StepVerifier.create(Mono.when(
                orderRepository.deleteAll(),
                cartItemRepository.deleteAll(),
                itemRepository.deleteAll()
        )).verifyComplete();
    }

    @Test
    void cart_buyButtonEnabled_whenBalanceEnough() {
        PAYMENTS.resetBalance(1_000);

        StepVerifier.create(
                itemRepository.save(new ItemEntity("A", "a", "/images/a.jpg", 500L))
                        .flatMap(item -> cartItemRepository.save(new CartItemEntity(item.getId(), 1)).then())
        ).verifyComplete();

        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> {
                    assertBuyButtonDisabled(html, false);
                    assertThat(html).doesNotContain("Недостаточно средств");
                    assertThat(html).doesNotContain("Платёжный сервис недоступен");
                });
    }

    @Test
    void cart_buyButtonDisabled_whenBalanceInsufficient() {
        PAYMENTS.resetBalance(100);

        StepVerifier.create(
                itemRepository.save(new ItemEntity("A", "a", "/images/a.jpg", 500L))
                        .flatMap(item -> cartItemRepository.save(new CartItemEntity(item.getId(), 1)).then())
        ).verifyComplete();

        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> {
                    assertBuyButtonDisabled(html, true);
                    assertThat(html).contains("Недостаточно средств на балансе.");
                });
    }

    @Test
    void cart_buyButtonDisabled_whenPaymentsServiceUnavailable() {
        PAYMENTS.setAvailable(false);

        StepVerifier.create(
                itemRepository.save(new ItemEntity("A", "a", "/images/a.jpg", 500L))
                        .flatMap(item -> cartItemRepository.save(new CartItemEntity(item.getId(), 1)).then())
        ).verifyComplete();

        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> {
                    assertBuyButtonDisabled(html, true);
                    assertThat(html).contains("Платёжный сервис недоступен.");
                });
    }

    @Test
    void buy_redirectsToCart_withInsufficientFunds_andDoesNotCreateOrder() {
        PAYMENTS.resetBalance(100);

        StepVerifier.create(
                itemRepository.save(new ItemEntity("A", "a", "/images/a.jpg", 500L))
                        .flatMap(item -> cartItemRepository.save(new CartItemEntity(item.getId(), 1)).then())
        ).verifyComplete();

        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().value("Location",
                        loc -> assertThat(loc).contains("/cart/items?buyError=INSUFFICIENT_FUNDS"));

        StepVerifier.create(orderRepository.count())
                .expectNext(0L)
                .verifyComplete();
    }

    @Test
    void buy_redirectsToCart_withServiceUnavailable_andDoesNotCreateOrder() {
        PAYMENTS.setAvailable(false);

        StepVerifier.create(
                itemRepository.save(new ItemEntity("A", "a", "/images/a.jpg", 500L))
                        .flatMap(item -> cartItemRepository.save(new CartItemEntity(item.getId(), 1)).then())
        ).verifyComplete();

        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().value("Location",
                        loc -> assertThat(loc).contains("/cart/items?buyError=SERVICE_UNAVAILABLE"));

        StepVerifier.create(orderRepository.count())
                .expectNext(0L)
                .verifyComplete();
    }

    private static final class PaymentsStubServer {

        private static final Pattern AMOUNT_PATTERN = Pattern.compile("\"amount\"\\s*:\\s*(\\d+)");

        private final AtomicBoolean available = new AtomicBoolean(true);
        private final AtomicLong balance = new AtomicLong(10_000);

        private volatile DisposableServer server;

        private static long parseAmount(String body) {
            Matcher m = AMOUNT_PATTERN.matcher(body);
            if (!m.find()) {
                return -1;
            }
            try {
                return Long.parseLong(m.group(1));
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        void startIfNeeded() {
            if (server != null) {
                return;
            }

            synchronized (this) {
                if (server != null) {
                    return;
                }

                server = HttpServer.create()
                        .host("localhost")
                        .port(0)
                        .route(routes -> routes
                                .get("/api/payments/balance", (req, res) -> {
                                    if (!available.get()) {
                                        return res.status(503)
                                                .header("Content-Type", "application/json")
                                                .sendString(Mono.just("{\"success\":false,\"balance\":0,\"message\":\"SERVICE_UNAVAILABLE\"}"))
                                                .then();
                                    }
                                    long b = balance.get();
                                    return res.status(200)
                                            .header("Content-Type", "application/json")
                                            .sendString(Mono.just("{\"balance\":" + b + "}"))
                                            .then();
                                })
                                .post("/api/payments/pay", (req, res) -> {
                                    if (!available.get()) {
                                        return res.status(503)
                                                .header("Content-Type", "application/json")
                                                .sendString(Mono.just("{\"success\":false,\"balance\":0,\"message\":\"SERVICE_UNAVAILABLE\"}"))
                                                .then();
                                    }

                                    return req.receive()
                                            .aggregate()
                                            .asString()
                                            .defaultIfEmpty("{}")
                                            .flatMap(body -> {
                                                long amount = parseAmount(body);

                                                if (amount <= 0) {
                                                    return res.status(400)
                                                            .header("Content-Type", "application/json")
                                                            .sendString(Mono.just("{\"success\":false,\"balance\":" + balance.get() + ",\"message\":\"INVALID_AMOUNT\"}"))
                                                            .then();
                                                }

                                                while (true) {
                                                    long current = balance.get();
                                                    long next = current - amount;

                                                    if (next < 0) {
                                                        return res.status(400)
                                                                .header("Content-Type", "application/json")
                                                                .sendString(Mono.just("{\"success\":false,\"balance\":" + current + ",\"message\":\"INSUFFICIENT_FUNDS\"}"))
                                                                .then();
                                                    }

                                                    if (balance.compareAndSet(current, next)) {
                                                        return res.status(200)
                                                                .header("Content-Type", "application/json")
                                                                .sendString(Mono.just("{\"success\":true,\"balance\":" + next + ",\"message\":\"OK\"}"))
                                                                .then();
                                                    }
                                                }
                                            });
                                })
                        )
                        .bindNow();
            }
        }

        void stop() {
            DisposableServer s = server;
            if (s != null) {
                s.disposeNow();
                server = null;
            }
        }

        String baseUrl() {
            DisposableServer s = server;
            if (s == null) {
                throw new IllegalStateException("server is not started");
            }
            return "http://localhost:" + s.port();
        }

        void setAvailable(boolean value) {
            available.set(value);
        }

        void resetBalance(long value) {
            balance.set(value);
        }
    }
}
