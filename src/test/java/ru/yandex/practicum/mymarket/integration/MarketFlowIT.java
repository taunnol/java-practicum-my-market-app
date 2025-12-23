package ru.yandex.practicum.mymarket.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.yandex.practicum.mymarket.cart.repo.CartItemRepository;
import ru.yandex.practicum.mymarket.items.model.ItemEntity;
import ru.yandex.practicum.mymarket.items.repo.ItemRepository;
import ru.yandex.practicum.mymarket.orders.repo.OrderRepository;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MarketFlowIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void cleanup() {
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        itemRepository.deleteAll();
    }

    @Test
    void flow_itemsToCartToBuyToOrder() throws Exception {
        ItemEntity ball = itemRepository.save(new ItemEntity(
                "Abobs",
                "aboba",
                "/images/aboba.jpg",
                1200L
        ));
        long itemId = ball.getId();

        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("paging"));

        mockMvc.perform(post("/items")
                        .param("id", String.valueOf(itemId))
                        .param("action", "PLUS")
                        .param("search", "")
                        .param("sort", "NO")
                        .param("pageNumber", "1")
                        .param("pageSize", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/items")));

        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attribute("items", hasSize(1)))
                .andExpect(model().attribute("total", equalTo(1200L)));

        MvcResult buyResult = mockMvc.perform(post("/buy"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String location = buyResult.getResponse().getHeader("Location");
        if (location == null) {
            throw new AssertionError("Redirect Location header is null");
        }

        Pattern p = Pattern.compile("/orders/(\\d+)\\?newOrder=true");
        Matcher m = p.matcher(location);
        if (!m.find()) {
            throw new AssertionError("Unexpected redirect location: " + location);
        }
        long orderId = Long.parseLong(m.group(1));

        mockMvc.perform(get("/orders/" + orderId).param("newOrder", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attribute("newOrder", equalTo(true)))
                .andExpect(model().attributeExists("order"))
                .andExpect(model().attribute("order", hasProperty("totalSum", equalTo(1200L))));
    }
}
