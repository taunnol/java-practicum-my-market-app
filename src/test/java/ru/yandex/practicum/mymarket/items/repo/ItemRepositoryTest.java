package ru.yandex.practicum.mymarket.items.repo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.mymarket.items.model.ItemEntity;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void findByTitleOrDescriptionContainingIgnoreCase_findsByTitle() {
        itemRepository.save(new ItemEntity("Aboba", "description 1", "/images/aboba.jpg", 1200L));
        itemRepository.save(new ItemEntity("Amoga", "description 1", "/images/amoga.jpg", 5900L));

        Page<ItemEntity> page = itemRepository
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                        "ab",
                        "ab",
                        PageRequest.of(0, 10)
                );

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst().getTitle()).isEqualTo("Aboba");
    }

    @Test
    void findByTitleOrDescriptionContainingIgnoreCase_findsByDescription() {
        itemRepository.save(new ItemEntity("Aboba", "Small aboba", "/images/aboba.jpg", 450L));
        itemRepository.save(new ItemEntity("Amoga", "Big amoga", "/images/amoga.jpg", 4500L));

        Page<ItemEntity> page = itemRepository
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                        "big",
                        "big",
                        PageRequest.of(0, 10)
                );

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst().getTitle()).isEqualTo("Amoga");
    }

    @Test
    void pagingAndSorting_works() {
        itemRepository.save(new ItemEntity("B", "d", "/images/1.jpg", 300L));
        itemRepository.save(new ItemEntity("A", "d", "/images/2.jpg", 100L));
        itemRepository.save(new ItemEntity("C", "d", "/images/3.jpg", 200L));

        Page<ItemEntity> page = itemRepository.findAll(
                PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "price"))
        );

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent().get(0).getPrice()).isEqualTo(100L);
        assertThat(page.getContent().get(1).getPrice()).isEqualTo(200L);
        assertThat(page.hasNext()).isTrue();
    }
}
