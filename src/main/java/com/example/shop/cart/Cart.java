package com.example.shop.cart;

import com.example.shop.common.Money;
import java.util.List;
import java.util.stream.Stream;

/**
 * Immutable aggregate. Every "change" returns a NEW Cart and leaves the original untouched.
 * One cart per user (the owner is the username).
 */
public record Cart(String owner, List<CartItem> items) {

    public Cart {
        items = List.copyOf(items); // defensive copy: the list can never be modified from outside
    }

    public static Cart emptyFor(String owner) {
        return new Cart(owner, List.of());
    }

    public Cart add(CartItem item) {
        boolean alreadyInCart = items.stream().anyMatch(i -> i.productId() == item.productId());
        List<CartItem> updated = alreadyInCart
                ? items.stream()
                        .map(i -> i.productId() == item.productId() ? i.plusQuantity(item.quantity()) : i)
                        .toList()
                : Stream.concat(items.stream(), Stream.of(item)).toList();
        return new Cart(owner, updated);
    }

    public Cart remove(long productId) {
        return new Cart(owner, items.stream().filter(i -> i.productId() != productId).toList());
    }

    public Cart cleared() {
        return emptyFor(owner);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public Money total() {
        return items.stream().map(CartItem::lineTotal).reduce(Money.ZERO, Money::plus);
    }
}
