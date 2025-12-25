package ru.yandex.practicum.mymarket.cart.repo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.mymarket.cart.model.CartItemEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class CartItemRepositoryTest {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Test
    void findByItemId_returnsEntity_whenExists() {
        cartItemRepository.saveAndFlush(new CartItemEntity(10L, 2));

        Optional<CartItemEntity> found = cartItemRepository.findByItemId(10L);

        assertThat(found).isPresent();
        assertThat(found.get().getItemId()).isEqualTo(10L);
        assertThat(found.get().getCount()).isEqualTo(2);
    }

    @Test
    void deleteByItemId_removesRow() {
        cartItemRepository.saveAndFlush(new CartItemEntity(10L, 2));

        cartItemRepository.deleteByItemId(10L);
        cartItemRepository.flush();

        assertThat(cartItemRepository.findByItemId(10L)).isEmpty();
    }

    @Test
    void uniqueConstraint_preventsTwoRowsWithSameItemId() {
        cartItemRepository.saveAndFlush(new CartItemEntity(10L, 1));

        assertThatThrownBy(() -> cartItemRepository.saveAndFlush(new CartItemEntity(10L, 2)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
