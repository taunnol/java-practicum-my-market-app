package ru.yandex.practicum.mymarket.payments.service;

public interface BalanceService {

    long getBalance();

    boolean tryWithdraw(long amount);
}
