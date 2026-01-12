package ru.yandex.practicum.mymarket.payments;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.yandex.practicum.mymarket.payments.service.InMemoryBalanceService;

import java.util.Map;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
class PaymentsServiceIT {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private InMemoryBalanceService balanceService;

    @BeforeEach
    void reset() {
        balanceService.resetToInitial();
    }

    @Test
    void balance_returnsInitialBalance() {
        webTestClient.get()
                .uri("/api/payments/balance")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.balance").isEqualTo(100);
    }

    @Test
    void pay_withEnoughFunds_returns200_andDecreasesBalance() {
        webTestClient.post()
                .uri("/api/payments/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("amount", 40))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.balance").isEqualTo(60)
                .jsonPath("$.message").isEqualTo("OK");
    }

    @Test
    void pay_withInsufficientFunds_returns400_andKeepsBalance() {
        webTestClient.post()
                .uri("/api/payments/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("amount", 200))
                .exchange()
                .expectStatus().isEqualTo(400)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.balance").isEqualTo(100)
                .jsonPath("$.message").isEqualTo("INSUFFICIENT_FUNDS");
    }
}