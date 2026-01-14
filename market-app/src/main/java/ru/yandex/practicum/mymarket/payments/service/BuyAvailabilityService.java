package ru.yandex.practicum.mymarket.payments.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class BuyAvailabilityService {

    private final PaymentsGateway gateway;

    public BuyAvailabilityService(PaymentsGateway gateway) {
        this.gateway = gateway;
    }

    public Mono<BuyAvailability> getBuyAvailability(long total) {
        if (total <= 0) {
            return Mono.just(BuyAvailability.blocked("Корзина пуста."));
        }

        return gateway.getBalance()
                .map(balance -> {
                    if (balance >= total) {
                        return BuyAvailability.ok();
                    }
                    return BuyAvailability.blocked("Недостаточно средств на балансе.");
                })
                .onErrorReturn(BuyAvailability.blocked("Платёжный сервис недоступен."));
    }
}
