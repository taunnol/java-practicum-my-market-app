package ru.yandex.practicum.mymarket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseCookie;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.security.web.server.authentication.logout.SecurityContextServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.WebSessionServerLogoutHandler;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;
import java.time.Duration;
import java.util.Map;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private static void expireCookie(ServerWebExchange exchange, String cookieName) {
        ResponseCookie cookie = ResponseCookie.from(cookieName, "")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();
        exchange.getResponse().addCookie(cookie);
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/webjars/**",
                                "/favicon.ico"
                        ).permitAll()
                        .pathMatchers(HttpMethod.GET, "/", "/items", "/items/**").permitAll()
                        .pathMatchers("/login", "/logout").permitAll()
                        .anyExchange().authenticated()
                )
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(Customizer.withDefaults())
                .logout(logout -> {
                    logout.requiresLogout(ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, "/logout"));
                    logout.logoutHandler(new SecurityContextServerLogoutHandler());
                    logout.logoutHandler(new WebSessionServerLogoutHandler());

                    RedirectServerLogoutSuccessHandler successHandler = new RedirectServerLogoutSuccessHandler();
                    successHandler.setLogoutSuccessUrl(URI.create("/"));

                    logout.logoutSuccessHandler((webFilterExchange, authentication) -> {
                        expireCookie(webFilterExchange.getExchange(), "SESSION");
                        expireCookie(webFilterExchange.getExchange(), "JSESSIONID");
                        expireCookie(webFilterExchange.getExchange(), "XSRF-TOKEN");
                        return successHandler.onLogoutSuccess(webFilterExchange, authentication);
                    });
                })
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
        DelegatingPasswordEncoder delegating = new DelegatingPasswordEncoder(
                "bcrypt",
                Map.of("bcrypt", bcrypt)
        );
        delegating.setDefaultPasswordEncoderForMatches(bcrypt);
        return delegating;
    }
}
