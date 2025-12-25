package ru.yandex.practicum.mymarket;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class MyMarketAppApplicationTests {

    @Test
    void contextLoads() {
    }
}
