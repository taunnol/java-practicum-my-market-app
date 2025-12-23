package ru.yandex.practicum.mymarket.orders.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mymarket.common.exception.NotFoundException;
import ru.yandex.practicum.mymarket.orders.dto.OrderDto;
import ru.yandex.practicum.mymarket.orders.dto.OrderItemDto;
import ru.yandex.practicum.mymarket.orders.model.OrderEntity;
import ru.yandex.practicum.mymarket.orders.model.OrderItemEntity;
import ru.yandex.practicum.mymarket.orders.repo.OrderRepository;

import java.util.Comparator;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    private static OrderDto toDto(OrderEntity order) {
        List<OrderItemDto> items = order.getItems().stream()
                .map(OrderService::toDto)
                .toList();

        long total = 0L;
        for (OrderItemDto i : items) {
            total += i.price() * (long) i.count();
        }

        return new OrderDto(order.getId(), items, total);
    }

    private static OrderItemDto toDto(OrderItemEntity e) {
        return new OrderItemDto(
                e.getItemId(),
                e.getTitle(),
                e.getPrice(),
                e.getCount()
        );
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getOrders() {
        return orderRepository.findAll().stream()
                .sorted(Comparator.comparing(OrderEntity::getId).reversed())
                .map(OrderService::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderDto getOrder(long id) {
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found: " + id));
        return toDto(order);
    }
}
