package com.example.shop.order;

import com.example.shop.cart.Cart;
import com.example.shop.cart.CartRepository;
import com.example.shop.common.NotFoundException;
import com.example.shop.payment.PaymentCommand;
import com.example.shop.payment.PaymentResult;
import com.example.shop.payment.PaymentService;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * FACADE over the checkout steps. It orchestrates only: it contains no payment-method logic and no SQL.
 *
 * Note: payment runs inside the transaction to keep the sample simple. If the processor throws (for example
 * invalid payment details) everything rolls back, so nothing half-finished is stored. In production,
 * call the gateway outside a long transaction and reconcile with webhooks.
 */
@Service
public class CheckoutService {

    private final CartRepository carts;
    private final OrderRepository orders;
    private final PaymentService payments;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    public CheckoutService(CartRepository carts, OrderRepository orders, PaymentService payments,
                           ApplicationEventPublisher events, Clock clock) {
        this.carts = carts;
        this.orders = orders;
        this.payments = payments;
        this.events = events;
        this.clock = clock;
    }

    @Transactional
    public Order checkout(String owner, String paymentMethod, Map<String, String> paymentDetails) {
        Cart cart = carts.findByOwner(owner).orElseGet(() -> Cart.emptyFor(owner));

        Order pending = orders.save(Order.place(cart, clock)); // throws if the cart is empty

        PaymentResult result = payments.pay(
                new PaymentCommand(pending.id(), pending.total(), paymentMethod, paymentDetails));

        // Pattern-matching switch over a sealed type: the compiler guarantees every outcome is handled.
        Order settled = switch (result) {
            case PaymentResult.Approved approved -> pending.markPaid();
            case PaymentResult.Declined declined -> pending.markPaymentFailed();
            case PaymentResult.Pending waiting -> pending.markAwaitingConfirmation();
        };

        if (settled.status() != OrderStatus.PAYMENT_FAILED) {
            carts.save(cart.cleared()); // keep the cart on a failed payment so the user can retry
        }

        Order saved = orders.save(settled);
        events.publishEvent(new OrderPlacedEvent(saved.id(), owner, saved.status()));
        return saved;
    }

    @Transactional(readOnly = true)
    public Order get(String owner, long orderId) {
        return orders.findByIdAndOwner(orderId, owner)
                .orElseThrow(() -> new NotFoundException("Order " + orderId + " not found"));
    }

    @Transactional(readOnly = true)
    public List<Order> list(String owner) {
        return orders.findByOwner(owner);
    }
}
