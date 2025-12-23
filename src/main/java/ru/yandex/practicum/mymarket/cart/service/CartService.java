package ru.yandex.practicum.mymarket.cart.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mymarket.cart.model.CartItemEntity;
import ru.yandex.practicum.mymarket.cart.repo.CartItemRepository;
import ru.yandex.practicum.mymarket.common.dto.CartAction;

import java.util.HashMap;
import java.util.Map;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;

    public CartService(CartItemRepository cartItemRepository) {
        this.cartItemRepository = cartItemRepository;
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
        if (itemId <= 0) {
            return;
        }

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
}
