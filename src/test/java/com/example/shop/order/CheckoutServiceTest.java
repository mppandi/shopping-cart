package com.example.shop.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.shop.cart.Cart;
import com.example.shop.cart.CartItem;
import com.example.shop.cart.CartRepository;
import com.example.shop.common.BusinessRuleException;
import com.example.shop.common.Money;
import com.example.shop.payment.PaymentCommand;
import com.example.shop.payment.PaymentProcessor;
import com.example.shop.payment.PaymentProcessorRegistry;
import com.example.shop.payment.PaymentResult;
import com.example.shop.payment.PaymentService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Pure unit test: no Spring, no database. In-memory fakes implement the ports. */
class CheckoutServiceTest {

    // ---- fakes -------------------------------------------------------------------------------------------
    static class InMemoryCarts implements CartRepository {
        final Map<String, Cart> store = new HashMap<>();

        public Optional<Cart> findByOwner(String owner) {
            return Optional.ofNullable(store.get(owner));
        }

        public Cart save(Cart cart) {
            store.put(cart.owner(), cart);
            return cart;
        }
    }

    static class InMemoryOrders implements OrderRepository {
        final Map<Long, Order> store = new HashMap<>();
        long nextId = 1;

        public Order save(Order order) {
            Order saved = order.id() == null
                    ? new Order(nextId++, order.owner(), order.lines(), order.total(), order.status(), order.placedAt())
                    : order;
            store.put(saved.id(), saved);
            return saved;
        }

        public Optional<Order> findByIdAndOwner(long id, String owner) {
            return Optional.ofNullable(store.get(id)).filter(o -> o.owner().equals(owner));
        }

        public List<Order> findByOwner(String owner) {
            return store.values().stream().filter(o -> o.owner().equals(owner)).toList();
        }
    }

    /** A processor whose answer the test decides. */
    static class ScriptedProcessor implements PaymentProcessor {
        PaymentResult next = new PaymentResult.Approved("TXN-1");

        public String method() {
            return "CARD";
        }

        public PaymentResult pay(PaymentCommand command) {
            return next;
        }
    }

    // ---- setup -------------------------------------------------------------------------------------------
    private final InMemoryCarts carts = new InMemoryCarts();
    private final InMemoryOrders orders = new InMemoryOrders();
    private final ScriptedProcessor card = new ScriptedProcessor();
    private final List<Object> publishedEvents = new ArrayList<>();
    private CheckoutService service;

    @BeforeEach
    void setUp() {
        service = new CheckoutService(
                carts,
                orders,
                new PaymentService(new PaymentProcessorRegistry(List.of(card))),
                publishedEvents::add,
                Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC));
        carts.save(Cart.emptyFor("alice").add(new CartItem(1, "Mouse", Money.of("19.99"), 2)));
    }

    // ---- tests -------------------------------------------------------------------------------------------
    @Test
    void approvedPayment_marksOrderPaid_andEmptiesCart() {
        Order order = service.checkout("alice", "CARD", Map.of("cardToken", "tok_ok"));

        assertThat(order.status()).isEqualTo(OrderStatus.PAID);
        assertThat(order.total()).isEqualTo(Money.of("39.98"));
        assertThat(carts.findByOwner("alice").orElseThrow().isEmpty()).isTrue();
    }

    @Test
    void declinedPayment_failsOrder_butKeepsCartForRetry() {
        card.next = new PaymentResult.Declined("no funds");

        Order order = service.checkout("alice", "CARD", Map.of());

        assertThat(order.status()).isEqualTo(OrderStatus.PAYMENT_FAILED);
        assertThat(carts.findByOwner("alice").orElseThrow().items()).hasSize(1);
    }

    @Test
    void pendingPayment_awaitsConfirmation_andEmptiesCart() {
        card.next = new PaymentResult.Pending("REF-1");

        Order order = service.checkout("alice", "CARD", Map.of());

        assertThat(order.status()).isEqualTo(OrderStatus.AWAITING_CONFIRMATION);
        assertThat(carts.findByOwner("alice").orElseThrow().isEmpty()).isTrue();
    }

    @Test
    void emptyCart_isRejected_andNothingIsSaved() {
        assertThatThrownBy(() -> service.checkout("bob", "CARD", Map.of()))
                .isInstanceOf(BusinessRuleException.class);
        assertThat(orders.store).isEmpty();
    }

    @Test
    void unknownPaymentMethod_isRejected() {
        assertThatThrownBy(() -> service.checkout("alice", "BITCOIN", Map.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void checkout_publishesAnOrderPlacedEvent() {
        service.checkout("alice", "CARD", Map.of());

        assertThat(publishedEvents).containsExactly(new OrderPlacedEvent(1L, "alice", OrderStatus.PAID));
    }

    @Test
    void orders_areOnlyVisibleToTheirOwner() {
        Order order = service.checkout("alice", "CARD", Map.of());

        assertThat(service.list("alice")).hasSize(1);
        assertThat(service.list("bob")).isEmpty();
        assertThatThrownBy(() -> service.get("bob", order.id()))
                .isInstanceOf(com.example.shop.common.NotFoundException.class);
    }
}
