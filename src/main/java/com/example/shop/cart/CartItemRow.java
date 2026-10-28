package com.example.shop.cart;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

/** One row of the cart_items table (Hibernate 6 supports records as embeddables). */
@Embeddable
public record CartItemRow(
        long productId,
        String name,
        @Column(precision = 19, scale = 2) BigDecimal unitPrice,
        int quantity) {}
