package ru.yandex.practicum.mymarket.payments.service;

public record BuyAvailability(
        boolean canBuy,
        String message
) {
    public static BuyAvailability ok() {
        return new BuyAvailability(true, null);
    }

    public static BuyAvailability blocked(String message) {
        return new BuyAvailability(false, message);
    }
}
