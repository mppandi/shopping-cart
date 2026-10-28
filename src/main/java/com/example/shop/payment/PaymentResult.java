package com.example.shop.payment;

/**
 * The outcomes of a payment are a CLOSED set, so the type is sealed and the compiler
 * checks that every switch handles all of them. (The set of payment METHODS is open: see PaymentProcessor.)
 */
public sealed interface PaymentResult {

    record Approved(String transactionId) implements PaymentResult {}

    record Declined(String reason) implements PaymentResult {}

    /** Accepted but not yet confirmed, for example a UPI collect request the customer must approve. */
    record Pending(String reference) implements PaymentResult {}
}
