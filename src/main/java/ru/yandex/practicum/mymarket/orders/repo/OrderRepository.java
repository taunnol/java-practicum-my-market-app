package ru.yandex.practicum.mymarket.orders.repo;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.mymarket.orders.model.OrderEntity;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    @Override
    @EntityGraph(attributePaths = "items")
    List<OrderEntity> findAll();

    @Override
    @EntityGraph(attributePaths = "items")
    Optional<OrderEntity> findById(Long id);
}
