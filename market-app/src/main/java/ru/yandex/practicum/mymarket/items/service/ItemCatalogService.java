package ru.yandex.practicum.mymarket.items.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.cart.service.CartService;
import ru.yandex.practicum.mymarket.common.dto.Paging;
import ru.yandex.practicum.mymarket.common.dto.SortMode;
import ru.yandex.practicum.mymarket.common.exception.NotFoundException;
import ru.yandex.practicum.mymarket.common.util.ImgPathUtils;
import ru.yandex.practicum.mymarket.items.cache.CachedItem;
import ru.yandex.practicum.mymarket.items.cache.CachedItemService;
import ru.yandex.practicum.mymarket.items.dto.ItemDto;

import java.util.List;
import java.util.Map;

@Service
public class ItemCatalogService {

    private final CachedItemService cachedItemService;
    private final CartService cartService;

    public ItemCatalogService(CachedItemService cachedItemService, CartService cartService) {
        this.cachedItemService = cachedItemService;
        this.cartService = cartService;
    }

    private static ItemDto mapItem(CachedItem ci, int count) {
        return new ItemDto(
                ci.id(),
                ci.title(),
                ci.description(),
                ImgPathUtils.normalize(ci.imgPath()),
                ci.price(),
                count
        );
    }

    public Mono<CatalogPage> getCatalogPage(String search, SortMode sort, int pageNumber, int pageSize) {
        String q = (search == null) ? "" : search.trim();
        SortMode sortMode = (sort == null) ? SortMode.NO : sort;

        int offset = (pageNumber - 1) * pageSize;

        Mono<Map<Long, Integer>> countsMono = cartService.getCountsByItemId();

        return cachedItemService.getCatalogPage(q, sortMode, pageNumber, pageSize)
                .zipWith(countsMono, (cachedPage, counts) -> {
                    List<ItemDto> items = cachedPage.items().stream()
                            .map(ci -> mapItem(ci, counts.getOrDefault(ci.id(), 0)))
                            .toList();

                    long total = cachedPage.total();

                    boolean hasPrevious = pageNumber > 1;
                    boolean hasNext = (long) offset + (long) pageSize < total;

                    Paging paging = new Paging(
                            pageSize,
                            pageNumber,
                            hasPrevious,
                            hasNext
                    );

                    return new CatalogPage(items, paging);
                });
    }

    public Mono<ItemDto> getItem(long id) {
        return cachedItemService.getItem(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Item not found: " + id)))
                .zipWith(cartService.getCountsByItemId(), (ci, counts) ->
                        mapItem(ci, counts.getOrDefault(ci.id(), 0))
                );
    }
}
