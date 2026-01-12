package ru.yandex.practicum.mymarket.items.cache;

import java.util.List;

public record CachedCatalogPage(
        List<CachedItem> items,
        long total
) {
}
