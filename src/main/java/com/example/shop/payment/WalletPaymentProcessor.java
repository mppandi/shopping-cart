package com.example.shop.payment;

import com.example.shop.common.Money;
import org.springframework.stereotype.Component;

/** Simulated wallet. Demo rule: a wallet balance is 500.00, so bigger orders are declined. */
@Component
public class WalletPaymentProcessor implements PaymentProcessor {

    private static final Money DEMO_BALANCE = Money.of("500.00");

    @Override
    public String method() {
        return "WALLET";
    }

    @Override
    public PaymentResult pay(PaymentCommand command) {
        String walletId = command.details().get("walletId");
        if (walletId == null || walletId.isBlank()) {
            throw new IllegalArgumentException("Payment detail 'walletId' is required for WALLET");
        }
        if (command.amount().amount().compareTo(DEMO_BALANCE.amount()) > 0) {
            return new PaymentResult.Declined("Insufficient wallet balance");
        }
        return new PaymentResult.Approved("WALLET-" + command.orderId());
    }
}
