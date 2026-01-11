package ru.yandex.practicum.mymarket.items.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.cart.service.CartService;
import ru.yandex.practicum.mymarket.common.dto.SortMode;
import ru.yandex.practicum.mymarket.items.model.ItemEntity;
import ru.yandex.practicum.mymarket.items.repo.ItemRepository;
import ru.yandex.practicum.mymarket.testutil.TestEntityIds;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemCatalogServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CartService cartService;

    @InjectMocks
    private ItemCatalogService itemCatalogService;

    @Captor
    private ArgumentCaptor<Integer> limitCaptor;

    @Captor
    private ArgumentCaptor<Integer> offsetCaptor;

    @Captor
    private ArgumentCaptor<SortMode> sortCaptor;

    @Test
    void getCatalogPage_mapsCounts_andCalculatesPaging() {
        ItemEntity e1 = new ItemEntity("A", "d", "/img", 100L);
        ItemEntity e2 = new ItemEntity("B", "d", "/img", 200L);
        TestEntityIds.setId(e1, 10L);
        TestEntityIds.setId(e2, 20L);

        when(cartService.getCountsByItemId()).thenReturn(Mono.just(Map.of(10L, 2)));
        when(itemRepository.countBySearch("")).thenReturn(Mono.just(2L));
        when(itemRepository.findPage(eq(""), any(), anyInt(), anyInt())).thenReturn(Flux.just(e1, e2));

        StepVerifier.create(itemCatalogService.getCatalogPage("", SortMode.NO, 1, 5))
                .assertNext(page -> {
                    assertThat(page.items()).hasSize(2);
                    assertThat(page.items().get(0).id()).isEqualTo(10L);
                    assertThat(page.items().get(0).count()).isEqualTo(2);
                    assertThat(page.items().get(1).id()).isEqualTo(20L);
                    assertThat(page.items().get(1).count()).isEqualTo(0);

                    assertThat(page.paging().pageNumber()).isEqualTo(1);
                    assertThat(page.paging().pageSize()).isEqualTo(5);
                    assertThat(page.paging().hasPrevious()).isFalse();
                    assertThat(page.paging().hasNext()).isFalse();
                })
                .verifyComplete();
    }

    @Test
    void getCatalogPage_passesSortLimitOffset() {
        when(cartService.getCountsByItemId()).thenReturn(Mono.just(Map.of()));
        when(itemRepository.countBySearch("q")).thenReturn(Mono.just(100L));
        when(itemRepository.findPage(eq("q"), any(), anyInt(), anyInt())).thenReturn(Flux.empty());

        StepVerifier.create(itemCatalogService.getCatalogPage(" q ", SortMode.PRICE, 2, 20))
                .assertNext(page -> assertThat(page.paging().hasPrevious()).isTrue())
                .verifyComplete();

        verify(itemRepository).findPage(eq("q"), sortCaptor.capture(), limitCaptor.capture(), offsetCaptor.capture());
        assertThat(sortCaptor.getValue()).isEqualTo(SortMode.PRICE);
        assertThat(limitCaptor.getValue()).isEqualTo(20);
        assertThat(offsetCaptor.getValue()).isEqualTo(20);
    }

    @Test
    void getItem_returnsDtoWithCount() {
        ItemEntity e = new ItemEntity("Aboba", "aboba", "/images/aboba.jpg", 2100L);
        TestEntityIds.setId(e, 7L);

        when(itemRepository.findById(7L)).thenReturn(Mono.just(e));
        when(cartService.getCountsByItemId()).thenReturn(Mono.just(Map.of(7L, 3)));

        StepVerifier.create(itemCatalogService.getItem(7L))
                .assertNext(dto -> {
                    assertThat(dto.id()).isEqualTo(7L);
                    assertThat(dto.count()).isEqualTo(3);
                    assertThat(dto.imgPath()).isEqualTo("images/aboba.jpg");
                })
                .verifyComplete();
    }
}
