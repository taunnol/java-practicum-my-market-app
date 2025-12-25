package ru.yandex.practicum.mymarket.items.service;

import ru.yandex.practicum.mymarket.common.dto.Paging;
import ru.yandex.practicum.mymarket.items.dto.ItemDto;

import java.util.List;

public record CatalogPage(
        List<ItemDto> items,
        Paging paging
) {
}
