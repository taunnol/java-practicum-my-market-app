package ru.yandex.practicum.mymarket.items.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mymarket.cart.service.CartService;
import ru.yandex.practicum.mymarket.common.dto.Paging;
import ru.yandex.practicum.mymarket.common.dto.SortMode;
import ru.yandex.practicum.mymarket.common.exception.NotFoundException;
import ru.yandex.practicum.mymarket.common.util.ImgPathUtils;
import ru.yandex.practicum.mymarket.items.dto.ItemDto;
import ru.yandex.practicum.mymarket.items.model.ItemEntity;
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

    private static Sort toSort(SortMode sortMode) {
        return switch (sortMode) {
            case NO -> Sort.unsorted();
            case ALPHA -> Sort.by(Sort.Direction.ASC, "title");
            case PRICE -> Sort.by(Sort.Direction.ASC, "price");
        };
    }

    private static ItemDto toDto(ItemEntity e, Map<Long, Integer> counts) {
        long id = e.getId();
        int count = counts.getOrDefault(id, 0);
        return new ItemDto(
                id,
                e.getTitle(),
                e.getDescription(),
                ImgPathUtils.normalize(e.getImgPath()),
                e.getPrice(),
                count
        );
    }

    @Transactional(readOnly = true)
    public CatalogPage getCatalogPage(String search, SortMode sort, int pageNumber, int pageSize) {
        String q = (search == null) ? "" : search.trim();
        SortMode sortMode = (sort == null) ? SortMode.NO : sort;

        int safePageSize = (pageSize <= 0) ? 5 : pageSize;
        int safePageNumber = (pageNumber <= 0) ? 1 : pageNumber;

        Pageable pageable = PageRequest.of(
                safePageNumber - 1,
                safePageSize,
                toSort(sortMode)
        );

        Page<ItemEntity> page;
        if (q.isEmpty()) {
            page = itemRepository.findAll(pageable);
        } else {
            page = itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    q, q, pageable
            );
        }

        Map<Long, Integer> counts = cartService.getCountsByItemId();

        List<ItemDto> items = page.getContent().stream()
                .map(e -> toDto(e, counts))
                .toList();

        Paging paging = new Paging(
                safePageSize,
                safePageNumber,
                page.hasPrevious(),
                page.hasNext()
        );

        return new CatalogPage(items, paging);
    }

    @Transactional(readOnly = true)
    public ItemDto getItem(long id) {
        ItemEntity entity = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Item not found: " + id));

        Map<Long, Integer> counts = cartService.getCountsByItemId();
        return toDto(entity, counts);
    }
}
