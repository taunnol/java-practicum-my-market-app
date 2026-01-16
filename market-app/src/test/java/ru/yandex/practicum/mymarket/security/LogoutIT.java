package ru.yandex.practicum.mymarket.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.yandex.practicum.mymarket.testsupport.MyMarketSpringBootTest;
import ru.yandex.practicum.mymarket.testutil.TestAuth;

import static org.assertj.core.api.Assertions.assertThat;

@MyMarketSpringBootTest
class LogoutIT {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void afterLogout_privateEndpointsRedirectToLogin() {
        WebTestClient auth = TestAuth.login(webTestClient, "user", "password");

        auth.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk();

        auth.post()
                .uri("/logout")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().value("Location", loc -> assertThat(loc).isEqualTo("/"));

        auth.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().value("Location", loc -> assertThat(loc).contains("/login"));
    }
}
