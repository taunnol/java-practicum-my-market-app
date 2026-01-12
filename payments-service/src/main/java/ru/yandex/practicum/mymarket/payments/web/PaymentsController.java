package ru.yandex.practicum.mymarket.payments.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.payments.openapi.api.PaymentsApi;
import ru.yandex.practicum.mymarket.payments.openapi.model.BalanceResponse;
import ru.yandex.practicum.mymarket.payments.openapi.model.PaymentRequest;
import ru.yandex.practicum.mymarket.payments.openapi.model.PaymentResponse;
import ru.yandex.practicum.mymarket.payments.service.BalanceService;

@RestController
public class PaymentsController implements PaymentsApi {

    private final BalanceService balanceService;

    public PaymentsController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @Override
    public Mono<ResponseEntity<BalanceResponse>> getBalance(ServerWebExchange exchange) {
        BalanceResponse body = new BalanceResponse();
        body.setBalance(balanceService.getBalance());
        return Mono.just(ResponseEntity.ok(body));
    }

    @Override
    public Mono<ResponseEntity<PaymentResponse>> pay(Mono<PaymentRequest> paymentRequest, ServerWebExchange exchange) {
        return paymentRequest.map(req -> {
            long amount = req.getAmount();

            PaymentResponse body = new PaymentResponse();

            boolean ok;
            try {
                ok = balanceService.tryWithdraw(amount);
            } catch (IllegalArgumentException e) {
                body.setSuccess(false);
                body.setBalance(balanceService.getBalance());
                body.setMessage("INVALID_AMOUNT");
                return ResponseEntity.badRequest().body(body);
            }

            body.setSuccess(ok);
            body.setBalance(balanceService.getBalance());

            if (ok) {
                body.setMessage("OK");
                return ResponseEntity.ok(body);
            }

            body.setMessage("INSUFFICIENT_FUNDS");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
        });
    }
}
