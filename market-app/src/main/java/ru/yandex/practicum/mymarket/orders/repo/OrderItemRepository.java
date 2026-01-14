package ru.yandex.practicum.mymarket.orders.repo;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.mymarket.orders.model.OrderItemEntity;

public interface OrderItemRepository extends ReactiveCrudRepository<OrderItemEntity, Long> {

    Flux<OrderItemEntity> findAllByOrderId(Long orderId);
}
