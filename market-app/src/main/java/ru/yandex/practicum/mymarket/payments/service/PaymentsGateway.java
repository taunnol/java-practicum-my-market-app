package ru.yandex.practicum.mymarket.payments.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.payments.client.model.BalanceResponse;
import ru.yandex.practicum.mymarket.payments.client.model.PaymentRequest;
import ru.yandex.practicum.mymarket.payments.client.model.PaymentResponse;
import ru.yandex.practicum.mymarket.payments.config.PaymentsProperties;

import java.time.Duration;

@Component
public class PaymentsGateway {

    private final WebClient webClient;
    private final Duration timeout;

    public PaymentsGateway(WebClient paymentsWebClient, PaymentsProperties properties) {
        this.webClient = paymentsWebClient;
        Duration t = properties.timeout();
        if (t == null) {
            t = Duration.ofMillis(500);
        }
        this.timeout = t;
    }

    public Mono<Long> getBalance() {
        return webClient.get()
                .uri("/api/payments/balance")
                .retrieve()
                .bodyToMono(BalanceResponse.class)
                .timeout(timeout)
                .map(BalanceResponse::getBalance);
    }

    public Mono<ResponseEntity<PaymentResponse>> payWithHttpInfo(long amount) {
        PaymentRequest req = new PaymentRequest();
        req.setAmount(amount);

        return webClient.post()
                .uri("/api/payments/pay")
                .bodyValue(req)
                .exchangeToMono(this::toPaymentResponseEntity)
                .timeout(timeout);
    }

    private Mono<ResponseEntity<PaymentResponse>> toPaymentResponseEntity(ClientResponse resp) {
        int code = resp.statusCode().value();

        if (code == 200 || code == 400) {
            return resp.bodyToMono(PaymentResponse.class)
                    .defaultIfEmpty(new PaymentResponse())
                    .map(body -> ResponseEntity.status(resp.statusCode()).body(body));
        }

        return Mono.just(ResponseEntity.status(resp.statusCode()).build());
    }
}
