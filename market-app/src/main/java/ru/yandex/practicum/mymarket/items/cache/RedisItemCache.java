package ru.yandex.practicum.mymarket.items.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;

@Service
public class RedisItemCache {

    private final ReactiveValueOperations<String, String> ops;
    private final ObjectMapper objectMapper;
    private final ItemsCacheProperties props;

    public RedisItemCache(ReactiveStringRedisTemplate template, ObjectMapper objectMapper, ItemsCacheProperties props) {
        this.ops = template.opsForValue();
        this.objectMapper = objectMapper;
        this.props = props;
    }

    private static String itemKey(long id) {
        return "items:card:" + id;
    }

    private static String catalogKey(CatalogCacheKey key) {
        String raw = key.searchNormalized()
                + "|" + key.sortMode().name()
                + "|" + key.pageNumber()
                + "|" + key.pageSize();
        String hash = DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
        return "items:list:" + hash;
    }

    public Mono<CachedItem> getItem(long id) {
        return ops.get(itemKey(id))
                .flatMap(json -> fromJson(json, CachedItem.class));
    }

    public Mono<Void> putItem(long id, CachedItem item) {
        return toJson(item)
                .flatMap(json -> ops.set(itemKey(id), json, props.ttl()))
                .then();
    }

    public Mono<CachedCatalogPage> getCatalogPage(CatalogCacheKey key) {
        return ops.get(catalogKey(key))
                .flatMap(json -> fromJson(json, CachedCatalogPage.class));
    }

    public Mono<Void> putCatalogPage(CatalogCacheKey key, CachedCatalogPage page) {
        return toJson(page)
                .flatMap(json -> ops.set(catalogKey(key), json, props.ttl()))
                .then();
    }

    private <T> Mono<String> toJson(T value) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(value))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private <T> Mono<T> fromJson(String json, Class<T> type) {
        return Mono.fromCallable(() -> objectMapper.readValue(json, type))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
