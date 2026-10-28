package com.example.shop.common;

/** A business rule was violated (for example an empty cart at checkout). Maps to HTTP 409. */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
