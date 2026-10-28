package com.example.shop.payment;

/**
 * STRATEGY + extension point (Open/Closed principle).
 * To support a new payment method, add one class that implements this interface and annotate it with @Component.
 * Nothing else in the application changes.
 */
public interface PaymentProcessor {

    /** Unique code such as CARD, UPI or WALLET (case-insensitive). */
    String method();

    PaymentResult pay(PaymentCommand command);
}
