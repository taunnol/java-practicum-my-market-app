package ru.yandex.practicum.mymarket.cart.web;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.cart.service.CartService;

@Controller
@Validated
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping({"/cart", "/cart/items"})
    public Mono<String> getCart(Model model) {
        return renderCart(model);
    }

    @PostMapping("/cart/items")
    public Mono<String> changeCart(
            @Valid @ModelAttribute CartActionForm form,
            Model model
    ) {
        return cartService.changeCount(form.getId(), form.getAction())
                .then(renderCart(model));
    }

    private Mono<String> renderCart(Model model) {
        return cartService.getCartView()
                .map(view -> {
                    model.addAttribute("items", view.items());
                    model.addAttribute("total", view.total());
                    return "cart";
                });
    }
}
