package ru.yandex.practicum.mymarket.cart.repo;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.cart.model.CartItemEntity;

public interface CartItemRepository extends ReactiveCrudRepository<CartItemEntity, Long> {

    Mono<CartItemEntity> findByItemId(Long itemId);

    Mono<Long> deleteByItemId(Long itemId);
}
