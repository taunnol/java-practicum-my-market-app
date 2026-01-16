package ru.yandex.practicum.mymarket.orders.repo;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.orders.model.OrderEntity;

public interface OrderRepository extends ReactiveCrudRepository<OrderEntity, Long> {

    Flux<OrderEntity> findAllByUserIdOrderByIdDesc(Long userId);

    Mono<OrderEntity> findByIdAndUserId(Long id, Long userId);
}
