package ru.yandex.practicum.mymarket.payments.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.mymarket.payments.client.api.PaymentsApi;
import ru.yandex.practicum.mymarket.payments.client.invoker.ApiClient;

@Configuration
@EnableConfigurationProperties(PaymentsProperties.class)
public class PaymentsClientConfig {

    @Bean
    public ApiClient paymentsApiClient(PaymentsProperties properties) {
        ApiClient apiClient = new ApiClient();

        String baseUrl = properties.baseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://localhost:8081";
        }
        apiClient.setBasePath(baseUrl);

        return apiClient;
    }

    @Bean
    public PaymentsApi paymentsApi(ApiClient apiClient) {
        PaymentsApi api = new PaymentsApi();
        api.setApiClient(apiClient);
        return api;
    }
}
