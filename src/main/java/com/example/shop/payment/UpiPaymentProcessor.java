package com.example.shop.payment;

import org.springframework.stereotype.Component;

/** Simulated UPI: creates a collect request and returns PENDING until the customer approves it. */
@Component
public class UpiPaymentProcessor implements PaymentProcessor {

    @Override
    public String method() {
        return "UPI";
    }

    @Override
    public PaymentResult pay(PaymentCommand command) {
        String vpa = command.details().get("vpa");
        if (vpa == null || !vpa.contains("@")) {
            throw new IllegalArgumentException("Payment detail 'vpa' (like name@bank) is required for UPI");
        }
        return new PaymentResult.Pending("UPI-REQ-" + command.orderId());
    }
}
