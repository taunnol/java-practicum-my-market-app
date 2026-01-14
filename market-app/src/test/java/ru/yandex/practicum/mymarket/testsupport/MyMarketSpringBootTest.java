package ru.yandex.practicum.mymarket.testsupport;

import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
public @interface MyMarketSpringBootTest {
}
