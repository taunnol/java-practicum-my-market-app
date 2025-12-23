package ru.yandex.practicum.mymarket.cart.dto;

import ru.yandex.practicum.mymarket.items.dto.ItemDto;

import java.util.List;

public record CartView(
        List<ItemDto> items,
        long total
) {
}
