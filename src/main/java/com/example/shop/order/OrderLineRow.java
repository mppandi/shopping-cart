package com.example.shop.order;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
public record OrderLineRow(
        long productId,
        String name,
        @Column(precision = 19, scale = 2) BigDecimal unitPrice,
        int quantity) {}
