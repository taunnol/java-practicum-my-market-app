package ru.yandex.practicum.mymarket.cart.web;

import jakarta.validation.constraints.Positive;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.mymarket.cart.dto.CartView;
import ru.yandex.practicum.mymarket.cart.service.CartService;
import ru.yandex.practicum.mymarket.common.dto.CartAction;

@Controller
@Validated
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    private String renderCart(Model model) {
        CartView view = cartService.getCartView();
        model.addAttribute("items", view.items());
        model.addAttribute("total", view.total());
        return "cart";
    }

    @GetMapping({"/cart", "/cart/items"})
    public String getCart(Model model) {
        return renderCart(model);
    }

    @PostMapping("/cart/items")
    public String changeCart(
            @RequestParam("id") @Positive long id,
            @RequestParam("action") CartAction action,
            Model model
    ) {
        cartService.changeCount(id, action);

        return renderCart(model);
    }
}
