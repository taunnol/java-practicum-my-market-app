package ru.yandex.practicum.mymarket.items.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.mymarket.items.model.ItemEntity;

public interface ItemRepository extends JpaRepository<ItemEntity, Long> {

    Page<ItemEntity> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String titlePart,
            String descriptionPart,
            Pageable pageable
    );
}
