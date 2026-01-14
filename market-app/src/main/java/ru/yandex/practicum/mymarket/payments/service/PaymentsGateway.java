package ru.yandex.practicum.mymarket.payments.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.payments.client.api.PaymentsApi;
import ru.yandex.practicum.mymarket.payments.client.model.BalanceResponse;
import ru.yandex.practicum.mymarket.payments.client.model.PaymentRequest;
import ru.yandex.practicum.mymarket.payments.client.model.PaymentResponse;
import ru.yandex.practicum.mymarket.payments.config.PaymentsProperties;

import java.time.Duration;

@Component
public class PaymentsGateway {

    private final PaymentsApi paymentsApi;
    private final Duration timeout;

    public PaymentsGateway(PaymentsApi paymentsApi, PaymentsProperties properties) {
        this.paymentsApi = paymentsApi;

        Duration t = properties.timeout();
        if (t == null) {
            t = Duration.ofMillis(500);
        }
        this.timeout = t;
    }

    public Mono<Long> getBalance() {
        return paymentsApi.getBalance()
                .timeout(timeout)
                .map(BalanceResponse::getBalance);
    }

    public Mono<ResponseEntity<PaymentResponse>> payWithHttpInfo(long amount) {
        PaymentRequest req = new PaymentRequest();
        req.setAmount(amount);

        return paymentsApi.payWithHttpInfo(req)
                .timeout(timeout);
    }
}
