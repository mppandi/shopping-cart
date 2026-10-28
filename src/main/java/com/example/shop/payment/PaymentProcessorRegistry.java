package com.example.shop.payment;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * FACTORY / REGISTRY: Spring injects every PaymentProcessor bean, and we index them by method code.
 * Duplicate codes fail at startup (IllegalStateException), so a clash is found immediately.
 */
@Component
public class PaymentProcessorRegistry {

    private final Map<String, PaymentProcessor> byMethod;

    public PaymentProcessorRegistry(List<PaymentProcessor> processors) {
        this.byMethod = processors.stream()
                .collect(Collectors.toUnmodifiableMap(p -> normalize(p.method()), Function.identity()));
    }

    public PaymentProcessor forMethod(String method) {
        PaymentProcessor processor = byMethod.get(normalize(method));
        if (processor == null) {
            throw new IllegalArgumentException(
                    "Unsupported payment method: " + method + ". Supported: " + supportedMethods());
        }
        return processor;
    }

    public List<String> supportedMethods() {
        return byMethod.keySet().stream().sorted().toList();
    }

    private static String normalize(String method) {
        return method == null ? "" : method.trim().toUpperCase(Locale.ROOT);
    }
}
