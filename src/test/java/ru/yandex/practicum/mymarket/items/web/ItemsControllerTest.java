package ru.yandex.practicum.mymarket.items.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.mymarket.cart.service.CartService;
import ru.yandex.practicum.mymarket.common.dto.CartAction;
import ru.yandex.practicum.mymarket.common.dto.Paging;
import ru.yandex.practicum.mymarket.common.util.GridUtils;
import ru.yandex.practicum.mymarket.items.dto.ItemDto;
import ru.yandex.practicum.mymarket.items.service.CatalogPage;
import ru.yandex.practicum.mymarket.items.service.ItemCatalogService;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemsController.class)
@ActiveProfiles("test")
class ItemsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemCatalogService itemCatalogService;

    @MockBean
    private CartService cartService;

    @Test
    void getItems_returnsItemsView_andModelAttributes() throws Exception {
        List<ItemDto> flat = List.of(
                new ItemDto(1, "t1", "d1", "images/1.jpg", 10, 0),
                new ItemDto(2, "t2", "d2", "images/2.jpg", 20, 1),
                new ItemDto(3, "t3", "d3", "images/3.jpg", 30, 0),
                new ItemDto(4, "t4", "d4", "images/4.jpg", 40, 0)
        );
        CatalogPage page = new CatalogPage(flat, new Paging(5, 1, false, true));
        when(itemCatalogService.getCatalogPage(anyString(), any(), anyInt(), anyInt())).thenReturn(page);

        List<List<ItemDto>> expectedRows = GridUtils.toRowsOf3WithPlaceholders(flat);

        mockMvc.perform(get("/items")
                        .param("search", "abc")
                        .param("sort", "NO")
                        .param("pageNumber", "1")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"))
                .andExpect(model().attribute("search", "abc"))
                .andExpect(model().attribute("sort", "NO"))
                .andExpect(model().attributeExists("paging"))
                .andExpect(model().attribute("items", equalTo(expectedRows)));
    }

    @Test
    void postItems_changesCount_andRedirectsBackKeepingQueryParams() throws Exception {
        mockMvc.perform(post("/items")
                        .param("id", "10")
                        .param("action", "PLUS")
                        .param("search", "q")
                        .param("sort", "PRICE")
                        .param("pageNumber", "2")
                        .param("pageSize", "20"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/items")))
                .andExpect(header().string("Location", containsString("search=q")))
                .andExpect(header().string("Location", containsString("sort=PRICE")))
                .andExpect(header().string("Location", containsString("pageNumber=2")))
                .andExpect(header().string("Location", containsString("pageSize=20")));

        verify(cartService).changeCount(10L, CartAction.PLUS);
    }

    @Test
    void getItems_invalidSort_returns400() throws Exception {
        mockMvc.perform(get("/items").param("sort", "BAD"))
                .andExpect(status().isBadRequest());
    }
}
