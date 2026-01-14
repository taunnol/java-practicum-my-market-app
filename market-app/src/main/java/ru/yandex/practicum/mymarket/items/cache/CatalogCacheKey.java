package ru.yandex.practicum.mymarket.items.cache;

import ru.yandex.practicum.mymarket.common.dto.SortMode;

public record CatalogCacheKey(
        String searchNormalized,
        SortMode sortMode,
        int pageNumber,
        int pageSize
) {
    public static CatalogCacheKey of(String search, SortMode sortMode, int pageNumber, int pageSize) {
        String q = (search == null) ? "" : search.trim();
        SortMode s = (sortMode == null) ? SortMode.NO : sortMode;
        return new CatalogCacheKey(q, s, pageNumber, pageSize);
    }
}
