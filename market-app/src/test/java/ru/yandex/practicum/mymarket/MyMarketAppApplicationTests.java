package ru.yandex.practicum.mymarket;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.mymarket.testsupport.MyMarketSpringBootTest;
import ru.yandex.practicum.mymarket.testsupport.RedisSpringBootTestBase;

@MyMarketSpringBootTest
class MyMarketAppApplicationTests extends RedisSpringBootTestBase {

    @Test
    void contextLoads() {
    }
}
