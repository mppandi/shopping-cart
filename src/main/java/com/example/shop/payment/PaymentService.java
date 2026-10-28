package com.example.shop.payment;

import org.springframework.stereotype.Service;

/** Knows nothing about CARD, UPI or WALLET: it only asks the registry for the right strategy. */
@Service
public class PaymentService {

    private final PaymentProcessorRegistry registry;

    public PaymentService(PaymentProcessorRegistry registry) {
        this.registry = registry;
    }

    public PaymentResult pay(PaymentCommand command) {
        return registry.forMethod(command.method()).pay(command);
    }
}
