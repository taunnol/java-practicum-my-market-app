package ru.yandex.practicum.mymarket.items.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.cart.service.CartService;
import ru.yandex.practicum.mymarket.common.dto.SortMode;
import ru.yandex.practicum.mymarket.items.cache.CachedCatalogPage;
import ru.yandex.practicum.mymarket.items.cache.CachedItem;
import ru.yandex.practicum.mymarket.items.cache.CachedItemService;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemCatalogServiceTest {

    @Mock
    private CachedItemService cachedItemService;

    @Mock
    private CartService cartService;

    @InjectMocks
    private ItemCatalogService itemCatalogService;

    @Captor
    private ArgumentCaptor<String> searchCaptor;

    @Captor
    private ArgumentCaptor<SortMode> sortCaptor;

    @Captor
    private ArgumentCaptor<Integer> pageNumberCaptor;

    @Captor
    private ArgumentCaptor<Integer> pageSizeCaptor;

    @Test
    void getCatalogPage_mapsCounts_andCalculatesPaging() {
        CachedItem i1 = new CachedItem(10L, "A", "d", "/img", 100L);
        CachedItem i2 = new CachedItem(20L, "B", "d", "/img", 200L);

        when(cartService.getCountsByItemId()).thenReturn(Mono.just(Map.of(10L, 2)));
        when(cachedItemService.getCatalogPage("", SortMode.NO, 1, 5))
                .thenReturn(Mono.just(new CachedCatalogPage(List.of(i1, i2), 2L)));

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
    void getCatalogPage_passesSortPageParams_andNormalizesSearch() {
        when(cartService.getCountsByItemId()).thenReturn(Mono.just(Map.of()));
        when(cachedItemService.getCatalogPage("q", SortMode.PRICE, 2, 20))
                .thenReturn(Mono.just(new CachedCatalogPage(List.of(), 100L)));

        StepVerifier.create(itemCatalogService.getCatalogPage(" q ", SortMode.PRICE, 2, 20))
                .assertNext(page -> {
                    assertThat(page.paging().hasPrevious()).isTrue();
                    assertThat(page.paging().hasNext()).isTrue();
                })
                .verifyComplete();

        verify(cachedItemService).getCatalogPage(
                searchCaptor.capture(),
                sortCaptor.capture(),
                pageNumberCaptor.capture(),
                pageSizeCaptor.capture()
        );

        assertThat(searchCaptor.getValue()).isEqualTo("q");
        assertThat(sortCaptor.getValue()).isEqualTo(SortMode.PRICE);
        assertThat(pageNumberCaptor.getValue()).isEqualTo(2);
        assertThat(pageSizeCaptor.getValue()).isEqualTo(20);
    }

    @Test
    void getItem_returnsDtoWithCount_andNormalizesImgPath() {
        CachedItem item = new CachedItem(7L, "Aboba", "aboba", "/images/aboba.jpg", 2100L);

        when(cachedItemService.getItem(7L)).thenReturn(Mono.just(item));
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
