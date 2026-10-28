package com.example.shop.order;

import com.example.shop.cart.Cart;
import com.example.shop.common.BusinessRuleException;
import com.example.shop.common.Money;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * Immutable aggregate. Status changes return a new Order and are only allowed from PENDING_PAYMENT.
 * The id is null until the order is first saved.
 */
public record Order(Long id, String owner, List<OrderLine> lines, Money total, OrderStatus status, Instant placedAt) {

    public Order {
        lines = List.copyOf(lines);
    }

    /** FACTORY METHOD: the only way to start a new order, so an invalid order cannot be built. */
    public static Order place(Cart cart, Clock clock) {
        if (cart.isEmpty()) {
            throw new BusinessRuleException("Cannot check out an empty cart");
        }
        return new Order(
                null,
                cart.owner(),
                cart.items().stream().map(OrderLine::from).toList(),
                cart.total(),
                OrderStatus.PENDING_PAYMENT,
                clock.instant());
    }

    public Order markPaid() {
        return moveTo(OrderStatus.PAID);
    }

    public Order markPaymentFailed() {
        return moveTo(OrderStatus.PAYMENT_FAILED);
    }

    public Order markAwaitingConfirmation() {
        return moveTo(OrderStatus.AWAITING_CONFIRMATION);
    }

    private Order moveTo(OrderStatus next) {
        if (status != OrderStatus.PENDING_PAYMENT) {
            throw new BusinessRuleException("Order is " + status + " and cannot become " + next);
        }
        return new Order(id, owner, lines, total, next, placedAt);
    }
}
