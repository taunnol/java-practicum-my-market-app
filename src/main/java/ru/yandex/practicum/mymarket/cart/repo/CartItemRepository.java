package ru.yandex.practicum.mymarket.cart.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.mymarket.cart.model.CartItemEntity;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {

    Optional<CartItemEntity> findByItemId(Long itemId);

    void deleteByItemId(Long itemId);
}
