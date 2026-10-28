package com.example.shop.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.shop.cart.Cart;
import com.example.shop.cart.CartItem;
import com.example.shop.common.BusinessRuleException;
import com.example.shop.common.Money;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class OrderTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);

    private static Cart cartWithOneItem() {
        return Cart.emptyFor("alice").add(new CartItem(1, "Mouse", Money.of("19.99"), 2));
    }

    @Test
    void place_copiesLinesAndTotal_andStartsPending() {
        Order order = Order.place(cartWithOneItem(), CLOCK);

        assertThat(order.status()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(order.total()).isEqualTo(Money.of("39.98"));
        assertThat(order.lines()).hasSize(1);
        assertThat(order.placedAt()).isEqualTo(Instant.parse("2026-01-01T10:00:00Z"));
    }

    @Test
    void place_emptyCart_isRejected() {
        assertThatThrownBy(() -> Order.place(Cart.emptyFor("alice"), CLOCK))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void markPaid_returnsNewOrder_andOriginalStaysPending() {
        Order pending = Order.place(cartWithOneItem(), CLOCK);

        Order paid = pending.markPaid();

        assertThat(paid.status()).isEqualTo(OrderStatus.PAID);
        assertThat(pending.status()).isEqualTo(OrderStatus.PENDING_PAYMENT);
    }

    @Test
    void settledOrder_cannotChangeStatusAgain() {
        Order paid = Order.place(cartWithOneItem(), CLOCK).markPaid();

        assertThatThrownBy(paid::markPaymentFailed).isInstanceOf(BusinessRuleException.class);
    }
}
