package ru.yandex.practicum.mymarket.items.cache;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.common.dto.SortMode;
import ru.yandex.practicum.mymarket.items.repo.ItemRepository;

import java.util.Collection;

@Service
public class CachedItemService {

    private final ItemRepository itemRepository;
    private final RedisItemCache redisItemCache;
    private final CacheErrorHandler cacheErrorHandler;

    public CachedItemService(ItemRepository itemRepository, RedisItemCache redisItemCache, CacheErrorHandler cacheErrorHandler) {
        this.itemRepository = itemRepository;
        this.redisItemCache = redisItemCache;
        this.cacheErrorHandler = cacheErrorHandler;
    }

    public Mono<CachedItem> getItem(long id) {
        return redisItemCache.getItem(id)
                .onErrorResume(e -> cacheErrorHandler.onReadError("items:card", "id=" + id, e))
                .switchIfEmpty(
                        itemRepository.findById(id)
                                .map(CachedItem::fromEntity)
                                .flatMap(ci -> redisItemCache.putItem(id, ci)
                                        .onErrorResume(e -> cacheErrorHandler.onWriteError("items:card", "id=" + id, e))
                                        .thenReturn(ci))
                );
    }

    public Flux<CachedItem> getItems(Collection<Long> ids) {
        return Flux.fromIterable(ids)
                .flatMap(this::getItem);
    }

    public Mono<CachedCatalogPage> getCatalogPage(String search, SortMode sort, int pageNumber, int pageSize) {
        CatalogCacheKey key = CatalogCacheKey.of(search, sort, pageNumber, pageSize);
        int offset = (pageNumber - 1) * pageSize;
        String keyForLog = key.toString();

        return redisItemCache.getCatalogPage(key)
                .onErrorResume(e -> cacheErrorHandler.onReadError("items:list", keyForLog, e))
                .switchIfEmpty(
                        Mono.zip(
                                        itemRepository.countBySearch(key.searchNormalized()),
                                        itemRepository.findPage(key.searchNormalized(), key.sortMode(), pageSize, offset)
                                                .map(CachedItem::fromEntity)
                                                .collectList()
                                )
                                .map(t -> new CachedCatalogPage(t.getT2(), t.getT1()))
                                .flatMap(page -> redisItemCache.putCatalogPage(key, page)
                                        .onErrorResume(e -> cacheErrorHandler.onWriteError("items:list", keyForLog, e))
                                        .thenReturn(page))
                );
    }
}
