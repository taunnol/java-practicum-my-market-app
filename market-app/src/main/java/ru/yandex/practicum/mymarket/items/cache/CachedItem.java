package ru.yandex.practicum.mymarket.items.cache;

import ru.yandex.practicum.mymarket.items.model.ItemEntity;

public record CachedItem(
        long id,
        String title,
        String description,
        String imgPath,
        long price
) {
    public static CachedItem fromEntity(ItemEntity e) {
        return new CachedItem(
                e.getId(),
                e.getTitle(),
                e.getDescription(),
                e.getImgPath(),
                e.getPrice()
        );
    }
}
