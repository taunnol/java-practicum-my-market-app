package ru.yandex.practicum.mymarket.users.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CurrentUserService {

    public Mono<Long> currentUserId() {
        return currentUserIdOrEmpty()
                .switchIfEmpty(Mono.error(new IllegalStateException("User is not authenticated")));
    }

    public Mono<Long> currentUserIdOrEmpty() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .flatMap(auth -> {
                    Object principal = auth.getPrincipal();
                    if (principal instanceof UserPrincipal up) {
                        return Mono.just(up.getId());
                    }
                    return Mono.empty();
                })
                .onErrorResume(e -> Mono.empty());
    }
}
