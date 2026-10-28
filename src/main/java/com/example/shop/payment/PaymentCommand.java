package com.example.shop.payment;

import com.example.shop.common.Money;
import java.util.Map;
import java.util.Objects;

/** Everything a processor needs. The order id doubles as the idempotency key. */
public record PaymentCommand(long orderId, Money amount, String method, Map<String, String> details) {

    public PaymentCommand {
        Objects.requireNonNull(amount, "amount");
        if (method == null || method.isBlank()) {
            throw new IllegalArgumentException("Payment method is required");
        }
        details = details == null ? Map.of() : Map.copyOf(details);
    }
}
