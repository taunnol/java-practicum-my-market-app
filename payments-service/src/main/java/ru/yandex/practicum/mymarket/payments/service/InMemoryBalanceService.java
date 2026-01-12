package ru.yandex.practicum.mymarket.payments.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.mymarket.payments.config.PaymentsProperties;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class InMemoryBalanceService implements BalanceService {

    private final PaymentsProperties properties;
    private final AtomicLong balance;

    public InMemoryBalanceService(PaymentsProperties properties) {
        this.properties = properties;
        this.balance = new AtomicLong(properties.initialBalance());
    }

    @Override
    public long getBalance() {
        return balance.get();
    }

    @Override
    public boolean tryWithdraw(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be > 0");
        }

        while (true) {
            long current = balance.get();
            long next = current - amount;
            if (next < 0) {
                return false;
            }
            if (balance.compareAndSet(current, next)) {
                return true;
            }
        }
    }

    public void resetToInitial() {
        balance.set(properties.initialBalance());
    }
}
