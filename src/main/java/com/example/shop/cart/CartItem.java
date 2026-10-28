package com.example.shop.cart;

import com.example.shop.common.Money;
import java.util.Objects;

/** Immutable. The unit price is a snapshot taken when the item was added. */
public record CartItem(long productId, String name, Money unitPrice, int quantity) {

    public static final int MAX_QUANTITY = 99;

    public CartItem {
        Objects.requireNonNull(unitPrice, "unitPrice");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Item name is required");
        }
        if (quantity < 1 || quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException("Quantity must be between 1 and " + MAX_QUANTITY);
        }
    }

    public Money lineTotal() {
        return unitPrice.times(quantity);
    }

    public CartItem plusQuantity(int extra) {
        return new CartItem(productId, name, unitPrice, quantity + extra);
    }
}
