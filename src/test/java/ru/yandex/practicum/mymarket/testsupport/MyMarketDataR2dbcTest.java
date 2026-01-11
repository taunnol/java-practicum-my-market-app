package ru.yandex.practicum.mymarket.testsupport;

import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.mymarket.config.R2dbcInitConfig;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@DataR2dbcTest
@Import(R2dbcInitConfig.class)
@ActiveProfiles("test")
public @interface MyMarketDataR2dbcTest {
}
