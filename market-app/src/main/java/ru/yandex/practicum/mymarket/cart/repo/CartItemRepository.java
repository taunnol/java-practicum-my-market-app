package ru.yandex.practicum.mymarket.cart.repo;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.cart.model.CartItemEntity;

public interface CartItemRepository extends ReactiveCrudRepository<CartItemEntity, Long> {

    Flux<CartItemEntity> findAllByUserId(Long userId);

    Mono<CartItemEntity> findByUserIdAndItemId(Long userId, Long itemId);

    Mono<Long> deleteByUserIdAndItemId(Long userId, Long itemId);

    Mono<Long> deleteAllByUserId(Long userId);
}
