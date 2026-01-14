package ru.yandex.practicum.mymarket.items.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeoutException;

@Component
public class CacheErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(CacheErrorHandler.class);

    private static boolean isRedisUnavailable(Throwable t) {
        if (t instanceof RedisConnectionFailureException) {
            return true;
        }
        if (t instanceof RedisSystemException) {
            Throwable c = t.getCause();
            return (c instanceof RedisConnectionFailureException);
        }
        return false;
    }

    private static boolean isTimeout(Throwable t) {
        if (t instanceof TimeoutException) {
            return true;
        }
        String name = t.getClass().getName();
        return name.endsWith("TimeoutException") || name.contains("Timeout");
    }

    private static boolean isSerializationProblem(Throwable t) {
        if (t instanceof JsonProcessingException) {
            return true;
        }
        return t instanceof SerializationException;
    }

    private static Throwable rootCause(Throwable t) {
        Throwable cur = t;
        for (int i = 0; i < 10; i++) {
            Throwable next = cur.getCause();
            if (next == null || next == cur) {
                return cur;
            }
            cur = next;
        }
        return cur;
    }

    public <T> Mono<T> onReadError(String cacheName, String cacheKey, Throwable error) {
        logCacheError("read", cacheName, cacheKey, error);
        return Mono.empty();
    }

    public Mono<Void> onWriteError(String cacheName, String cacheKey, Throwable error) {
        logCacheError("write", cacheName, cacheKey, error);
        return Mono.empty();
    }

    private void logCacheError(String op, String cacheName, String cacheKey, Throwable error) {
        Throwable root = rootCause(error);

        if (isRedisUnavailable(root) || isTimeout(root)) {
            log.warn("Cache {} failed ({}), key={}: {}", op, cacheName, cacheKey, root.toString());
            return;
        }

        if (isSerializationProblem(root)) {
            log.warn("Cache {} failed ({}), key={} (serialization): {}", op, cacheName, cacheKey, root.toString());
            return;
        }

        log.error("Cache {} failed ({}), key={}", op, cacheName, cacheKey, root);
    }
}
