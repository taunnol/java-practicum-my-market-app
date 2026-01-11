package ru.yandex.practicum.mymarket.cart.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.cart.dto.CartView;
import ru.yandex.practicum.mymarket.cart.model.CartItemEntity;
import ru.yandex.practicum.mymarket.cart.repo.CartItemRepository;
import ru.yandex.practicum.mymarket.common.dto.CartAction;
import ru.yandex.practicum.mymarket.items.dto.ItemDto;
import ru.yandex.practicum.mymarket.items.repo.ItemRepository;
import ru.yandex.practicum.mymarket.items.service.ItemDtoFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ItemRepository itemRepository;

    public CartService(CartItemRepository cartItemRepository, ItemRepository itemRepository) {
        this.cartItemRepository = cartItemRepository;
        this.itemRepository = itemRepository;
    }

    public Mono<Map<Long, Integer>> getCountsByItemId() {
        return cartItemRepository.findAll()
                .collectMap(CartItemEntity::getItemId, CartItemEntity::getCount);
    }

    public Mono<Void> changeCount(long itemId, CartAction action) {
        return cartItemRepository.findByItemId(itemId)
                .flatMap(entity -> switch (action) {
                    case PLUS -> {
                        entity.setCount(entity.getCount() + 1);
                        yield cartItemRepository.save(entity).thenReturn(entity);
                    }
                    case MINUS -> {
                        int next = entity.getCount() - 1;
                        if (next <= 0) {
                            yield cartItemRepository.delete(entity).thenReturn(entity);
                        }
                        entity.setCount(next);
                        yield cartItemRepository.save(entity).thenReturn(entity);
                    }
                    case DELETE -> cartItemRepository.delete(entity).thenReturn(entity);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    if (action == CartAction.PLUS) {
                        return cartItemRepository.save(new CartItemEntity(itemId, 1));
                    }
                    return Mono.empty();
                }))
                .then();
    }

    public Mono<Void> clear() {
        return cartItemRepository.deleteAll();
    }

    public Mono<CartView> getCartView() {
        return getCountsByItemId()
                .flatMap(counts -> {
                    if (counts.isEmpty()) {
                        return Mono.just(new CartView(List.of(), 0L));
                    }

                    return itemRepository.findAllById(counts.keySet())
                            .map(e -> ItemDtoFactory.fromEntity(e, counts.getOrDefault(e.getId(), 0)))
                            .collectSortedList(Comparator.comparingLong(ItemDto::id))
                            .map(items -> {
                                long total = 0L;
                                for (ItemDto item : items) {
                                    total += item.price() * (long) item.count();
                                }
                                return new CartView(items, total);
                            });
                });
    }
}
