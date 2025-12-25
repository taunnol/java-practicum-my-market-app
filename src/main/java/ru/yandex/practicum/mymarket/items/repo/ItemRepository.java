package ru.yandex.practicum.mymarket.items.repo;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import ru.yandex.practicum.mymarket.items.model.ItemEntity;

public interface ItemRepository extends ReactiveCrudRepository<ItemEntity, Long>, ItemRepositoryCustom {
}
