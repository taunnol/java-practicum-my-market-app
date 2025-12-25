package ru.yandex.practicum.mymarket.orders.dto;

import java.util.List;

public record OrderDto(
        long id,
        List<OrderItemDto> items,
        long totalSum
) {
}
