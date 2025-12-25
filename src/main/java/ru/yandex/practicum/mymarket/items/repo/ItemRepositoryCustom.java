package ru.yandex.practicum.mymarket.items.repo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.common.dto.SortMode;
import ru.yandex.practicum.mymarket.items.model.ItemEntity;

public interface ItemRepositoryCustom {

    Flux<ItemEntity> findPage(String search, SortMode sort, int limit, int offset);

    Mono<Long> countBySearch(String search);
}
