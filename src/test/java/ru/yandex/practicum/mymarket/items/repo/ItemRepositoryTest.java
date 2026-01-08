package ru.yandex.practicum.mymarket.items.repo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.common.dto.SortMode;
import ru.yandex.practicum.mymarket.items.model.ItemEntity;
import ru.yandex.practicum.mymarket.testsupport.MyMarketDataR2dbcTest;

import static org.assertj.core.api.Assertions.assertThat;

@MyMarketDataR2dbcTest
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    void cleanup() {
        StepVerifier.create(itemRepository.deleteAll()).verifyComplete();
    }

    @Test
    void countBySearch_countsFiltered() {
        Mono.when(
                itemRepository.save(new ItemEntity("Aboba", "description", "/i1", 100L)).then(),
                itemRepository.save(new ItemEntity("Amoga", "Big amoga", "/i2", 200L)).then(),
                itemRepository.save(new ItemEntity("Lamp", "Warm light", "/i3", 300L)).then()
        ).block();

        StepVerifier.create(itemRepository.countBySearch("am"))
                .assertNext(cnt -> assertThat(cnt).isEqualTo(2L))
                .verifyComplete();

        StepVerifier.create(itemRepository.countBySearch(""))
                .assertNext(cnt -> assertThat(cnt).isEqualTo(3L))
                .verifyComplete();
    }

    @Test
    void findPage_searchesAndSortsByPrice() {
        Mono.when(
                itemRepository.save(new ItemEntity("B", "d", "/i1", 300L)).then(),
                itemRepository.save(new ItemEntity("A", "d", "/i2", 100L)).then(),
                itemRepository.save(new ItemEntity("C", "d", "/i3", 200L)).then()
        ).block();

        StepVerifier.create(itemRepository.findPage("", SortMode.PRICE, 2, 0).collectList())
                .assertNext(list -> {
                    assertThat(list).hasSize(2);
                    assertThat(list.get(0).getPrice()).isEqualTo(100L);
                    assertThat(list.get(1).getPrice()).isEqualTo(200L);
                })
                .verifyComplete();
    }

    @Test
    void findPage_searchByTitleOrDescription_ignoreCase() {
        Mono.when(
                itemRepository.save(new ItemEntity("Aboba", "Small one", "/i1", 100L)).then(),
                itemRepository.save(new ItemEntity("Amoga", "Big amoga", "/i2", 200L)).then()
        ).block();

        StepVerifier.create(itemRepository.findPage("big", SortMode.NO, 10, 0).collectList())
                .assertNext(list -> {
                    assertThat(list).hasSize(1);
                    assertThat(list.getFirst().getTitle()).isEqualTo("Amoga");
                })
                .verifyComplete();
    }
}
