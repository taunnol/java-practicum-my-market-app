package ru.yandex.practicum.mymarket.orders.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.common.exception.NotFoundException;
import ru.yandex.practicum.mymarket.orders.dto.OrderDto;
import ru.yandex.practicum.mymarket.orders.dto.OrderItemDto;
import ru.yandex.practicum.mymarket.orders.model.OrderEntity;
import ru.yandex.practicum.mymarket.orders.model.OrderItemEntity;
import ru.yandex.practicum.mymarket.orders.repo.OrderItemRepository;
import ru.yandex.practicum.mymarket.orders.repo.OrderRepository;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    private static OrderItemDto toDto(OrderItemEntity e) {
        return new OrderItemDto(
                e.getItemId(),
                e.getTitle(),
                e.getPrice(),
                e.getCount()
        );
    }

    private static OrderDto toDto(OrderEntity order, List<OrderItemEntity> items) {
        List<OrderItemDto> dtoItems = items.stream()
                .map(OrderService::toDto)
                .toList();

        long total = 0L;
        for (OrderItemDto i : dtoItems) {
            total += i.price() * (long) i.count();
        }

        return new OrderDto(order.getId(), dtoItems, total);
    }

    public Mono<List<OrderDto>> getOrders() {
        return orderRepository.findAllByOrderByIdDesc()
                .flatMap(order ->
                        orderItemRepository.findAllByOrderId(order.getId())
                                .collectList()
                                .map(items -> toDto(order, items))
                )
                .collectList();
    }

    public Mono<OrderDto> getOrder(long id) {
        return orderRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Order not found: " + id)))
                .flatMap(order ->
                        orderItemRepository.findAllByOrderId(order.getId())
                                .collectList()
                                .map(items -> toDto(order, items))
                );
    }
}
