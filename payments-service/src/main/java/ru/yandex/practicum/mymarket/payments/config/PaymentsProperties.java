package ru.yandex.practicum.mymarket.payments.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payments")
public record PaymentsProperties(long initialBalance) {
}
