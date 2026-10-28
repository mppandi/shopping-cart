package com.example.shop.payment;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment-methods")
public class PaymentController {

    private final PaymentProcessorRegistry registry;

    public PaymentController(PaymentProcessorRegistry registry) {
        this.registry = registry;
    }

    /** Always matches what is deployed, because it is built from the registry. */
    @GetMapping
    public List<String> supportedMethods() {
        return registry.supportedMethods();
    }
}
