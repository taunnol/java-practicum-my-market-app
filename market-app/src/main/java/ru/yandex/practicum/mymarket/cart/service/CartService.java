package ru.yandex.practicum.mymarket.cart.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.cart.dto.CartView;
import ru.yandex.practicum.mymarket.cart.model.CartItemEntity;
import ru.yandex.practicum.mymarket.cart.repo.CartItemRepository;
import ru.yandex.practicum.mymarket.common.dto.CartAction;
import ru.yandex.practicum.mymarket.common.util.ImgPathUtils;
import ru.yandex.practicum.mymarket.items.cache.CachedItem;
import ru.yandex.practicum.mymarket.items.cache.CachedItemService;
import ru.yandex.practicum.mymarket.items.dto.ItemDto;
import ru.yandex.practicum.mymarket.users.security.CurrentUserService;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final CachedItemService cachedItemService;
    private final CurrentUserService currentUserService;

    public CartService(
            CartItemRepository cartItemRepository,
            CachedItemService cachedItemService,
            CurrentUserService currentUserService
    ) {
        this.cartItemRepository = cartItemRepository;
        this.cachedItemService = cachedItemService;
        this.currentUserService = currentUserService;
    }

    private static ItemDto toItemDto(CachedItem ci, int count) {
        return new ItemDto(
                ci.id(),
                ci.title(),
                ci.description(),
                ImgPathUtils.normalize(ci.imgPath()),
                ci.price(),
                count
        );
    }

    public Mono<Map<Long, Integer>> getCountsByItemId() {
        return currentUserService.currentUserIdOrEmpty()
                .flatMap(userId -> cartItemRepository.findAllByUserId(userId)
                        .collectMap(CartItemEntity::getItemId, CartItemEntity::getCount)
                )
                .switchIfEmpty(Mono.just(Map.of()));
    }

    public Mono<Void> changeCount(long itemId, CartAction action) {
        return currentUserService.currentUserId()
                .flatMap(userId -> cartItemRepository.findByUserIdAndItemId(userId, itemId)
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
                                return cartItemRepository.save(new CartItemEntity(userId, itemId, 1));
                            }
                            return Mono.empty();
                        }))
                        .then()
                );
    }

    public Mono<Void> clear() {
        return currentUserService.currentUserId()
                .flatMap(userId -> cartItemRepository.deleteAllByUserId(userId).then());
    }

    public Mono<CartView> getCartView() {
        return currentUserService.currentUserId()
                .flatMap(userId -> cartItemRepository.findAllByUserId(userId)
                        .collectMap(CartItemEntity::getItemId, CartItemEntity::getCount)
                        .flatMap(counts -> {
                            if (counts.isEmpty()) {
                                return Mono.just(new CartView(List.of(), 0L));
                            }

                            return cachedItemService.getItems(counts.keySet())
                                    .map(ci -> toItemDto(ci, counts.getOrDefault(ci.id(), 0)))
                                    .collectSortedList(Comparator.comparingLong(ItemDto::id))
                                    .map(items -> {
                                        long total = 0L;
                                        for (ItemDto item : items) {
                                            total += item.price() * (long) item.count();
                                        }
                                        return new CartView(items, total);
                                    });
                        })
                );
    }
}
