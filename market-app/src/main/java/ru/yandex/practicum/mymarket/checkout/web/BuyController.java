package ru.yandex.practicum.mymarket.checkout.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.checkout.service.CheckoutService;
import ru.yandex.practicum.mymarket.payments.service.InsufficientFundsException;
import ru.yandex.practicum.mymarket.payments.service.PaymentServiceUnavailableException;

@Controller
public class BuyController {

    private final CheckoutService checkoutService;

    public BuyController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/buy")
    public Mono<String> buy() {
        return checkoutService.buy()
                .map(id -> "redirect:/orders/" + id + "?newOrder=true")
                .onErrorResume(InsufficientFundsException.class,
                        e -> Mono.just("redirect:/cart/items?buyError=INSUFFICIENT_FUNDS"))
                .onErrorResume(PaymentServiceUnavailableException.class,
                        e -> Mono.just("redirect:/cart/items?buyError=SERVICE_UNAVAILABLE"));
    }
}
