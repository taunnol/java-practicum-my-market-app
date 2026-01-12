package ru.yandex.practicum.mymarket.payments.service;

public class PaymentServiceUnavailableException extends RuntimeException {
    public PaymentServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public PaymentServiceUnavailableException(String message) {
        super(message);
    }
}
