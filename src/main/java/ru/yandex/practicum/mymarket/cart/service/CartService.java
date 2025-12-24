package ru.yandex.practicum.mymarket.cart.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mymarket.cart.dto.CartView;
import ru.yandex.practicum.mymarket.cart.model.CartItemEntity;
import ru.yandex.practicum.mymarket.cart.repo.CartItemRepository;
import ru.yandex.practicum.mymarket.common.dto.CartAction;
import ru.yandex.practicum.mymarket.items.dto.ItemDto;
import ru.yandex.practicum.mymarket.items.model.ItemEntity;
import ru.yandex.practicum.mymarket.items.repo.ItemRepository;
import ru.yandex.practicum.mymarket.items.service.ItemDtoFactory;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;

    private final ItemRepository itemRepository;

    public CartService(CartItemRepository cartItemRepository, ItemRepository itemRepository) {
        this.cartItemRepository = cartItemRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional(readOnly = true)
    public Map<Long, Integer> getCountsByItemId() {
        Map<Long, Integer> result = new HashMap<>();
        for (CartItemEntity e : cartItemRepository.findAll()) {
            result.put(e.getItemId(), e.getCount());
        }
        return result;
    }

    @Transactional
    public void changeCount(long itemId, CartAction action) {
        CartItemEntity entity = cartItemRepository.findByItemId(itemId).orElse(null);

        switch (action) {
            case PLUS -> {
                if (entity == null) {
                    cartItemRepository.save(new CartItemEntity(itemId, 1));
                } else {
                    entity.setCount(entity.getCount() + 1);
                    cartItemRepository.save(entity);
                }
            }
            case MINUS -> {
                if (entity == null) {
                    return;
                }
                int next = entity.getCount() - 1;
                if (next <= 0) {
                    cartItemRepository.delete(entity);
                } else {
                    entity.setCount(next);
                    cartItemRepository.save(entity);
                }
            }
            case DELETE -> {
                if (entity != null) {
                    cartItemRepository.delete(entity);
                }
            }
        }
    }

    @Transactional
    public void clear() {
        cartItemRepository.deleteAllInBatch();
    }

    @Transactional(readOnly = true)
    public CartView getCartView() {
        List<CartItemEntity> cartRows = cartItemRepository.findAll();
        if (cartRows.isEmpty()) {
            return new CartView(List.of(), 0L);
        }

        Map<Long, Integer> counts = cartRows.stream()
                .collect(Collectors.toMap(CartItemEntity::getItemId, CartItemEntity::getCount));

        List<ItemEntity> entities = itemRepository.findAllById(counts.keySet());

        List<ItemDto> items = entities.stream()
                .map(e -> ItemDtoFactory.fromEntity(e, counts.getOrDefault(e.getId(), 0)))
                .sorted(Comparator.comparingLong(ItemDto::id))
                .toList();

        long total = 0L;
        for (ItemDto item : items) {
            total += item.price() * (long) item.count();
        }

        return new CartView(items, total);
    }
}
