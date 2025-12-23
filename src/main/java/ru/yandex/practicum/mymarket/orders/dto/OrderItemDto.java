package ru.yandex.practicum.mymarket.orders.dto;

public record OrderItemDto(
        long id,
        String title,
        long price,
        int count
) {
}
