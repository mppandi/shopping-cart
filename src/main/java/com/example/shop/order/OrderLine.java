package com.example.shop.order;

import com.example.shop.cart.CartItem;
import com.example.shop.common.Money;

/** A snapshot of what was bought, at the price it was bought for. */
public record OrderLine(long productId, String name, Money unitPrice, int quantity) {

    static OrderLine from(CartItem item) {
        return new OrderLine(item.productId(), item.name(), item.unitPrice(), item.quantity());
    }

    public Money lineTotal() {
        return unitPrice.times(quantity);
    }
}
