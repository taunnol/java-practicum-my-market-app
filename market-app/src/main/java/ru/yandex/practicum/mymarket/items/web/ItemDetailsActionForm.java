package ru.yandex.practicum.mymarket.items.web;

import jakarta.validation.constraints.NotNull;
import ru.yandex.practicum.mymarket.common.dto.ItemAction;

public class ItemDetailsActionForm {

    @NotNull
    private ItemAction action;

    public ItemAction getAction() {
        return action;
    }

    public void setAction(ItemAction action) {
        this.action = action;
    }
}
