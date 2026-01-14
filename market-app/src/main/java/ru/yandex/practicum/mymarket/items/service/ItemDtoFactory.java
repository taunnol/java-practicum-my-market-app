package ru.yandex.practicum.mymarket.items.service;

import ru.yandex.practicum.mymarket.common.util.ImgPathUtils;
import ru.yandex.practicum.mymarket.items.dto.ItemDto;
import ru.yandex.practicum.mymarket.items.model.ItemEntity;

public final class ItemDtoFactory {

    private ItemDtoFactory() {
    }

    public static ItemDto fromEntity(ItemEntity e, int count) {
        return new ItemDto(
                e.getId(),
                e.getTitle(),
                e.getDescription(),
                ImgPathUtils.normalize(e.getImgPath()),
                e.getPrice(),
                count
        );
    }
}
