package ru.yandex.practicum.mymarket.orders.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.mymarket.orders.dto.OrderDto;
import ru.yandex.practicum.mymarket.orders.dto.OrderItemDto;
import ru.yandex.practicum.mymarket.orders.service.OrderService;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrdersController.class)
@ActiveProfiles("test")
class OrdersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Test
    void getOrders_returnsOrdersView() throws Exception {
        when(orderService.getOrders()).thenReturn(List.of(
                new OrderDto(1, List.of(), 0),
                new OrderDto(2, List.of(), 0)
        ));

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attributeExists("orders"));
    }

    @Test
    void getOrder_returnsOrderView_withNewOrderFlag() throws Exception {
        when(orderService.getOrder(10L)).thenReturn(new OrderDto(
                10,
                List.of(new OrderItemDto(1, "A", 100, 2)),
                200
        ));

        mockMvc.perform(get("/orders/10").param("newOrder", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attributeExists("order"))
                .andExpect(model().attribute("newOrder", equalTo(true)));
    }
}
