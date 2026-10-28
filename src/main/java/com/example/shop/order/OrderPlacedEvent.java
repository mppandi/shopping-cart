package com.example.shop.order;

/** OBSERVER: published after checkout. Anything that cares (emails, analytics...) can listen without touching CheckoutService. */
public record OrderPlacedEvent(long orderId, String owner, OrderStatus status) {}
