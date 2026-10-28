package com.example.shop.payment;

import org.springframework.stereotype.Component;

/**
 * Simulated card gateway. Real systems send a gateway TOKEN (never the card number).
 * Demo rule: a token starting with "tok_fail" is declined, anything else is approved.
 */
@Component
public class CardPaymentProcessor implements PaymentProcessor {

    @Override
    public String method() {
        return "CARD";
    }

    @Override
    public PaymentResult pay(PaymentCommand command) {
        String token = required(command, "cardToken");
        if (token.startsWith("tok_fail")) {
            return new PaymentResult.Declined("Card declined by issuer");
        }
        return new PaymentResult.Approved("CARD-" + command.orderId());
    }

    private static String required(PaymentCommand command, String key) {
        String value = command.details().get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Payment detail '" + key + "' is required for CARD");
        }
        return value;
    }
}
