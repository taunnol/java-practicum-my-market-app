package ru.yandex.practicum.mymarket.cart.web;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.cart.service.CartService;
import ru.yandex.practicum.mymarket.payments.service.BuyAvailability;
import ru.yandex.practicum.mymarket.payments.service.PaymentsClient;

@Controller
@Validated
public class CartController {

    private final CartService cartService;
    private final PaymentsClient paymentsClient;

    public CartController(CartService cartService, PaymentsClient paymentsClient) {
        this.cartService = cartService;
        this.paymentsClient = paymentsClient;
    }

    private static String mapBuyError(String buyError) {
        if (buyError == null) {
            return null;
        }
        return switch (buyError) {
            case "INSUFFICIENT_FUNDS" -> "Недостаточно средств на балансе.";
            case "SERVICE_UNAVAILABLE" -> "Платёжный сервис недоступен.";
            default -> null;
        };
    }

    @GetMapping({"/cart", "/cart/items"})
    public Mono<String> getCart(
            @RequestParam(required = false) String buyError,
            Model model
    ) {
        return renderCart(model, buyError);
    }

    @PostMapping("/cart/items")
    public Mono<String> changeCart(
            @Valid @ModelAttribute CartActionForm form,
            Model model
    ) {
        return cartService.changeCount(form.getId(), form.getAction())
                .then(renderCart(model, null));
    }

    private Mono<String> renderCart(Model model, String buyError) {
        return cartService.getCartView()
                .flatMap(view -> {
                    Mono<BuyAvailability> availMono = paymentsClient.getBuyAvailability(view.total());

                    return availMono.map(avail -> {
                        boolean canBuy = !view.items().isEmpty() && avail.canBuy();
                        String message = avail.message();

                        String forcedMessage = mapBuyError(buyError);
                        if (forcedMessage != null) {
                            canBuy = false;
                            message = forcedMessage;
                        }

                        model.addAttribute("items", view.items());
                        model.addAttribute("total", view.total());
                        model.addAttribute("canBuy", canBuy);
                        model.addAttribute("buyMessage", message);

                        return "cart";
                    });
                });
    }
}
