package ru.yandex.practicum.mymarket.checkout.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.view.RedirectView;
import ru.yandex.practicum.mymarket.checkout.service.CheckoutService;

@Controller
public class BuyController {

    private final CheckoutService checkoutService;

    public BuyController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/buy")
    public RedirectView buy() {
        long id = checkoutService.buy();
        return new RedirectView("/orders/" + id + "?newOrder=true");
    }
}
