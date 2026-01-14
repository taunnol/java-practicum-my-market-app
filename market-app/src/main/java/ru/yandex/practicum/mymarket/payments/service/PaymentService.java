package ru.yandex.practicum.mymarket.payments.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class PaymentService {

    private final PaymentsGateway gateway;
    private final PaymentsErrorMapper errorMapper;

    public PaymentService(PaymentsGateway gateway, PaymentsErrorMapper errorMapper) {
        this.gateway = gateway;
        this.errorMapper = errorMapper;
    }

    public Mono<Void> pay(long amount) {
        if (amount <= 0) {
            return Mono.error(new IllegalArgumentException("amount must be > 0"));
        }

        return gateway.payWithHttpInfo(amount)
                .flatMap(errorMapper::mapPayResponse)
                .onErrorMap(errorMapper::mapPayThrowable);
    }
}
