package ru.yandex.practicum.mymarket.checkout.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.mymarket.checkout.service.CheckoutService;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BuyController.class)
@ActiveProfiles("test")
class BuyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CheckoutService checkoutService;

    @Test
    void buy_redirectsToNewOrderPage() throws Exception {
        when(checkoutService.buy()).thenReturn(123L);

        mockMvc.perform(post("/buy"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/orders/123?newOrder=true")));
    }
}
