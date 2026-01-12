package ru.yandex.practicum.mymarket.payments.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.payments.client.api.PaymentsApi;
import ru.yandex.practicum.mymarket.payments.client.model.BalanceResponse;
import ru.yandex.practicum.mymarket.payments.client.model.PaymentRequest;

import java.time.Duration;

@Service
public class PaymentsClient {

    private final PaymentsApi paymentsApi;
    private final Duration timeout;

    public PaymentsClient(PaymentsApi paymentsApi,
                          ru.yandex.practicum.mymarket.payments.config.PaymentsProperties properties) {
        this.paymentsApi = paymentsApi;

        Duration t = properties.timeout();
        if (t == null) {
            t = Duration.ofMillis(500);
        }
        this.timeout = t;
    }

    public Mono<BuyAvailability> getBuyAvailability(long total) {
        if (total <= 0) {
            return Mono.just(BuyAvailability.blocked("Корзина пуста."));
        }

        return getBalance()
                .map(balance -> {
                    if (balance >= total) {
                        return BuyAvailability.ok();
                    }
                    return BuyAvailability.blocked("Недостаточно средств на балансе.");
                })
                .onErrorReturn(BuyAvailability.blocked("Платёжный сервис недоступен."));
    }

    public Mono<Void> pay(long amount) {
        if (amount <= 0) {
            return Mono.error(new IllegalArgumentException("amount must be > 0"));
        }

        PaymentRequest req = new PaymentRequest();
        req.setAmount(amount);

        return paymentsApi.payWithHttpInfo(req)
                .timeout(timeout)
                .flatMap(resp -> mapPayResponse(resp))
                .onErrorMap(ex -> {
                    if (ex instanceof InsufficientFundsException) {
                        return ex;
                    }
                    if (ex instanceof IllegalArgumentException) {
                        return ex;
                    }
                    if (ex instanceof PaymentServiceUnavailableException) {
                        return ex;
                    }
                    return new PaymentServiceUnavailableException("Платёжный сервис недоступен.", ex);
                });
    }

    private Mono<Void> mapPayResponse(ResponseEntity<?> resp) {
        int code = resp.getStatusCode().value();
        if (code == 200) {
            return Mono.empty();
        }
        if (code == 400) {
            return Mono.error(new InsufficientFundsException("INSUFFICIENT_FUNDS"));
        }
        return Mono.error(new PaymentServiceUnavailableException("Payments returned HTTP " + code));
    }

    private Mono<Long> getBalance() {
        return paymentsApi.getBalance()
                .timeout(timeout)
                .map(BalanceResponse::getBalance);
    }
}
