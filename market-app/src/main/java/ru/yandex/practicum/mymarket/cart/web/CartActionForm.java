package ru.yandex.practicum.mymarket.cart.web;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ru.yandex.practicum.mymarket.common.dto.CartAction;

public class CartActionForm {

    @NotNull
    @Positive
    private Long id;

    @NotNull
    private CartAction action;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CartAction getAction() {
        return action;
    }

    public void setAction(CartAction action) {
        this.action = action;
    }
}
