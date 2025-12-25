package ru.yandex.practicum.mymarket.checkout.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yandex.practicum.mymarket.checkout.service.CheckoutService;

@Controller
public class BuyController {

    private final CheckoutService checkoutService;

    public BuyController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/buy")
    public String buy(RedirectAttributes attrs) {
        long id = checkoutService.buy();
        attrs.addAttribute("id", id);
        attrs.addAttribute("newOrder", true);
        return "redirect:/orders/{id}";
    }
}
