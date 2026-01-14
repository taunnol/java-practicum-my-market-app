package ru.yandex.practicum.mymarket.payments.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.payments.client.model.PaymentResponse;

@Component
public class PaymentsErrorMapper {

    public Mono<Void> mapPayResponse(ResponseEntity<PaymentResponse> resp) {
        int code = resp.getStatusCode().value();
        if (code == 200) {
            return Mono.empty();
        }
        if (code == 400) {
            return Mono.error(new InsufficientFundsException("INSUFFICIENT_FUNDS"));
        }
        if (code == 503) {
            return Mono.error(new PaymentServiceUnavailableException("Платёжный сервис недоступен."));
        }
        return Mono.error(new PaymentServiceUnavailableException("Payments returned HTTP " + code));
    }

    public Throwable mapPayThrowable(Throwable ex) {
        if (ex instanceof InsufficientFundsException) {
            return ex;
        }
        if (ex instanceof IllegalArgumentException) {
            return ex;
        }
        if (ex instanceof PaymentServiceUnavailableException) {
            return ex;
        }
        return new PaymentServiceUnavailableException("Платёжный сервис недоступен.", ex);
    }
}
