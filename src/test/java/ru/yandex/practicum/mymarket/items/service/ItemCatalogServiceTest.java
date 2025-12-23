package ru.yandex.practicum.mymarket.items.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.yandex.practicum.mymarket.cart.service.CartService;
import ru.yandex.practicum.mymarket.common.dto.SortMode;
import ru.yandex.practicum.mymarket.items.model.ItemEntity;
import ru.yandex.practicum.mymarket.items.repo.ItemRepository;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemCatalogServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CartService cartService;

    @InjectMocks
    private ItemCatalogService itemCatalogService;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    @Test
    void getCatalogPage_emptySearch_usesFindAll_andMapsCounts() {
        ItemEntity e1 = new ItemEntity("A", "d", "/img", 100L);
        ItemEntity e2 = new ItemEntity("B", "d", "/img", 200L);

        TestEntityIds.setId(e1, 10L);
        TestEntityIds.setId(e2, 20L);

        when(cartService.getCountsByItemId()).thenReturn(Map.of(10L, 2));
        when(itemRepository.findAll(any(Pageable.class))).thenReturn(
                new PageImpl<>(List.of(e1, e2), PageRequest.of(0, 5), 2)
        );

        CatalogPage page = itemCatalogService.getCatalogPage("", SortMode.NO, 1, 5);

        verify(itemRepository).findAll(pageableCaptor.capture());
        verify(itemRepository, never()).findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(any(), any(), any());

        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(5);
        assertThat(pageable.getSort().isUnsorted()).isTrue();

        assertThat(page.items()).hasSize(2);
        assertThat(page.items().get(0).id()).isEqualTo(10L);
        assertThat(page.items().get(0).count()).isEqualTo(2);
        assertThat(page.items().get(1).id()).isEqualTo(20L);
        assertThat(page.items().get(1).count()).isEqualTo(0);
    }

    @Test
    void getCatalogPage_nonEmptySearch_usesSearchMethod() {
        ItemEntity e1 = new ItemEntity("Lamp", "Warm light", "/img", 100L);
        TestEntityIds.setId(e1, 1L);

        when(cartService.getCountsByItemId()).thenReturn(Map.of());
        when(itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                eq("warm"), eq("warm"), any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(e1), PageRequest.of(0, 5), 1));

        CatalogPage page = itemCatalogService.getCatalogPage(" warm ", SortMode.NO, 1, 5);

        assertThat(page.items()).hasSize(1);
        verify(itemRepository, never()).findAll(any(Pageable.class));
        verify(itemRepository).findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                eq("warm"), eq("warm"), any(Pageable.class)
        );
    }

    @Test
    void getCatalogPage_sortAlpha_passesSortToRepository() {
        when(cartService.getCountsByItemId()).thenReturn(Map.of());
        when(itemRepository.findAll(any(Pageable.class))).thenReturn(
                new PageImpl<>(List.of(), PageRequest.of(0, 5), 0)
        );

        itemCatalogService.getCatalogPage("", SortMode.ALPHA, 1, 5);

        verify(itemRepository).findAll(pageableCaptor.capture());
        Sort sort = pageableCaptor.getValue().getSort();

        assertThat(sort.getOrderFor("title")).isNotNull();
        assertThat(sort.getOrderFor("title").isAscending()).isTrue();
    }

    @Test
    void getCatalogPage_sortPrice_passesSortToRepository() {
        when(cartService.getCountsByItemId()).thenReturn(Map.of());
        when(itemRepository.findAll(any(Pageable.class))).thenReturn(
                new PageImpl<>(List.of(), PageRequest.of(0, 5), 0)
        );

        itemCatalogService.getCatalogPage("", SortMode.PRICE, 1, 5);

        verify(itemRepository).findAll(pageableCaptor.capture());
        Sort sort = pageableCaptor.getValue().getSort();

        assertThat(sort.getOrderFor("price")).isNotNull();
        assertThat(sort.getOrderFor("price").isAscending()).isTrue();
    }

    @Test
    void getCatalogPage_paging_hasPreviousAndHasNext() {
        when(cartService.getCountsByItemId()).thenReturn(Map.of());

        PageRequest pr = PageRequest.of(1, 5);
        when(itemRepository.findAll(any(Pageable.class))).thenReturn(
                new PageImpl<>(List.of(), pr, 20)
        );

        CatalogPage page = itemCatalogService.getCatalogPage("", SortMode.NO, 2, 5);

        assertThat(page.paging().hasPrevious()).isTrue();
        assertThat(page.paging().hasNext()).isTrue();
        assertThat(page.paging().pageNumber()).isEqualTo(2);
        assertThat(page.paging().pageSize()).isEqualTo(5);
    }
}
