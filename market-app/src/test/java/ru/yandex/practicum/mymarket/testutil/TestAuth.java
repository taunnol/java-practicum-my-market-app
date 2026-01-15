package ru.yandex.practicum.mymarket.testutil;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import ru.yandex.practicum.mymarket.users.model.UserEntity;
import ru.yandex.practicum.mymarket.users.repo.UserRepository;

public final class TestAuth {

    private TestAuth() {
    }

    public static WebTestClient login(WebTestClient client, String username, String password) {
        FluxExchangeResult<Void> result = client.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("username", username)
                        .with("password", password))
                .exchange()
                .expectStatus().is3xxRedirection()
                .returnResult(Void.class);

        ResponseCookie session = result.getResponseCookies().getFirst("SESSION");
        if (session == null) {
            session = result.getResponseCookies().getFirst("JSESSIONID");
        }
        if (session == null) {
            throw new IllegalStateException("No session cookie after login");
        }

        return client.mutate()
                .defaultCookie(session.getName(), session.getValue())
                .build();
    }

    public static long userIdBlocking(UserRepository userRepository, String username) {
        Long id = userRepository.findByUsername(username)
                .map(UserEntity::getId)
                .block();

        if (id == null) {
            throw new IllegalStateException("User not found: " + username);
        }
        return id;
    }
}
