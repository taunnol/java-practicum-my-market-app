package ru.yandex.practicum.mymarket.items.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "mymarket.cache.items")
public record ItemsCacheProperties(Duration ttl) {

    public ItemsCacheProperties {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            ttl = Duration.ofMinutes(2);
        }
    }
}
