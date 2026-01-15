package ru.yandex.practicum.mymarket.payments.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Profile("test")
@EnableConfigurationProperties(PaymentsProperties.class)
public class PaymentsClientTestConfig {

    @Bean
    public WebClient paymentsWebClient(PaymentsProperties properties) {
        String baseUrl = properties.baseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://localhost:8081";
        }

        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
