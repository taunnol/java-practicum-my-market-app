package ru.yandex.practicum.mymarket.payments.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payments.security")
public record PaymentsSecurityProperties(String allowedClientId) {
}
