package ru.yandex.practicum.mymarket.items.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.cart.service.CartService;
import ru.yandex.practicum.mymarket.common.dto.Paging;
import ru.yandex.practicum.mymarket.common.dto.SortMode;
import ru.yandex.practicum.mymarket.common.exception.NotFoundException;
import ru.yandex.practicum.mymarket.items.dto.ItemDto;
import ru.yandex.practicum.mymarket.items.repo.ItemRepository;

import java.util.List;
import java.util.Map;

@Service
public class ItemCatalogService {

    private final ItemRepository itemRepository;
    private final CartService cartService;

    public ItemCatalogService(ItemRepository itemRepository, CartService cartService) {
        this.itemRepository = itemRepository;
        this.cartService = cartService;
    }

    public Mono<CatalogPage> getCatalogPage(String search, SortMode sort, int pageNumber, int pageSize) {
        String q = (search == null) ? "" : search.trim();
        SortMode sortMode = (sort == null) ? SortMode.NO : sort;

        int offset = (pageNumber - 1) * pageSize;

        Mono<Long> totalMono = itemRepository.countBySearch(q);
        Mono<Map<Long, Integer>> countsMono = cartService.getCountsByItemId();
        Mono<List<ItemDto>> itemsMono = itemRepository.findPage(q, sortMode, pageSize, offset)
                .collectList()
                .zipWith(countsMono, (entities, counts) -> entities.stream()
                        .map(e -> ItemDtoFactory.fromEntity(e, counts.getOrDefault(e.getId(), 0)))
                        .toList()
                );

        return Mono.zip(totalMono, itemsMono)
                .map(t -> {
                    long total = t.getT1();
                    List<ItemDto> items = t.getT2();

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
        return itemRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Item not found: " + id)))
                .flatMap(entity ->
                        cartService.getCountsByItemId()
                                .map(counts -> ItemDtoFactory.fromEntity(entity, counts.getOrDefault(entity.getId(), 0)))
                );
    }
}
