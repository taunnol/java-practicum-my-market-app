package ru.yandex.practicum.mymarket.payments.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "payments")
public record PaymentsProperties(
        String baseUrl,
        Duration timeout
) {
}
