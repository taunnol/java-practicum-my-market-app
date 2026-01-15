package ru.yandex.practicum.mymarket.items.cache;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.cart.repo.CartItemRepository;
import ru.yandex.practicum.mymarket.items.model.ItemEntity;
import ru.yandex.practicum.mymarket.items.repo.ItemRepository;
import ru.yandex.practicum.mymarket.orders.repo.OrderRepository;
import ru.yandex.practicum.mymarket.testsupport.MyMarketSpringBootTest;
import ru.yandex.practicum.mymarket.testsupport.RedisSpringBootTestBase;
import ru.yandex.practicum.mymarket.testutil.TestAuth;
import ru.yandex.practicum.mymarket.users.repo.UserRepository;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@MyMarketSpringBootTest
class RedisItemsCacheIT extends RedisSpringBootTestBase {

    @Autowired
    private WebTestClient webTestClient;

    @SpyBean
    private ItemRepository itemRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;

    @Autowired
    private UserRepository userRepository;

    @Test
    void catalogPage_isCachedInRedis_andSecondCallDoesNotHitDb() {
        StepVerifier.create(cleanupDb())
                .verifyComplete();

        StepVerifier.create(
                        itemRepository.saveAll(List.of(
                                        new ItemEntity("t1", "d1", "/images/1.jpg", 10L),
                                        new ItemEntity("t2", "d2", "/images/2.jpg", 20L),
                                        new ItemEntity("t3", "d3", "/images/3.jpg", 30L)
                                ))
                                .then()
                )
                .verifyComplete();

        clearInvocations(itemRepository);

        webTestClient.get()
                .uri("/items?search=&sort=NO&pageNumber=1&pageSize=5")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("Витрина магазина"));

        verify(itemRepository).countBySearch("");
        verify(itemRepository).findPage("", ru.yandex.practicum.mymarket.common.dto.SortMode.NO, 5, 0);

        StepVerifier.create(redisTemplate.keys("items:list:*").collectList())
                .assertNext(keys -> assertThat(keys).isNotEmpty())
                .verifyComplete();

        clearInvocations(itemRepository);

        webTestClient.get()
                .uri("/items?search=&sort=NO&pageNumber=1&pageSize=5")
                .exchange()
                .expectStatus().isOk();

        verify(itemRepository, never()).countBySearch(anyString());
        verify(itemRepository, never()).findPage(anyString(), any(), anyInt(), anyInt());
    }

    @Test
    void itemCard_isCachedInRedis_andSecondCallDoesNotHitDb() {
        StepVerifier.create(cleanupDb())
                .verifyComplete();

        AtomicLong idHolder = new AtomicLong();

        StepVerifier.create(
                        itemRepository.save(new ItemEntity("one", "desc", "/images/one.jpg", 111L))
                                .map(ItemEntity::getId)
                )
                .assertNext(idHolder::set)
                .verifyComplete();

        long id = idHolder.get();
        assertThat(id).isPositive();

        clearInvocations(itemRepository);

        webTestClient.get()
                .uri("/items/" + id)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("one"));

        verify(itemRepository).findById(id);

        StepVerifier.create(redisTemplate.hasKey("items:card:" + id))
                .expectNext(true)
                .verifyComplete();

        clearInvocations(itemRepository);

        webTestClient.get()
                .uri("/items/" + id)
                .exchange()
                .expectStatus().isOk();

        verify(itemRepository, never()).findById(id);
    }

    @Test
    void cart_usesCachedItems_andSecondCallDoesNotHitDb() {
        StepVerifier.create(cleanupDb())
                .verifyComplete();

        long userId = TestAuth.userIdBlocking(userRepository, "user");

        AtomicLong idHolder = new AtomicLong();

        StepVerifier.create(
                        itemRepository.save(new ItemEntity("cartItem", "desc", "/images/cart.jpg", 500L))
                                .map(ItemEntity::getId)
                )
                .assertNext(idHolder::set)
                .verifyComplete();

        long id = idHolder.get();
        assertThat(id).isPositive();

        StepVerifier.create(cartItemRepository.save(new ru.yandex.practicum.mymarket.cart.model.CartItemEntity(userId, id, 2)).then())
                .verifyComplete();

        WebTestClient auth = TestAuth.login(webTestClient, "user", "password");

        clearInvocations(itemRepository);

        auth.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("Корзина"));

        verify(itemRepository).findById(id);

        StepVerifier.create(redisTemplate.hasKey("items:card:" + id))
                .expectNext(true)
                .verifyComplete();

        clearInvocations(itemRepository);

        auth.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk();

        verify(itemRepository, never()).findById(id);
    }

    private Mono<Void> cleanupDb() {
        return Mono.when(
                orderRepository.deleteAll(),
                cartItemRepository.deleteAll(),
                itemRepository.deleteAll()
        ).then();
    }
}
