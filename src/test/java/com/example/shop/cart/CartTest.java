package com.example.shop.cart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.shop.common.Money;
import org.junit.jupiter.api.Test;

class CartTest {

    private static CartItem item(long productId, String price, int quantity) {
        return new CartItem(productId, "Product " + productId, Money.of(price), quantity);
    }

    @Test
    void add_returnsNewCart_andLeavesOriginalUnchanged() {
        Cart empty = Cart.emptyFor("alice");

        Cart updated = empty.add(item(1, "10.00", 2));

        assertThat(empty.items()).isEmpty();
        assertThat(updated.items()).hasSize(1);
    }

    @Test
    void add_sameProductTwice_mergesQuantity() {
        Cart cart = Cart.emptyFor("alice").add(item(1, "10.00", 2)).add(item(1, "10.00", 3));

        assertThat(cart.items()).hasSize(1);
        assertThat(cart.items().get(0).quantity()).isEqualTo(5);
    }

    @Test
    void total_sumsAllLines() {
        Cart cart = Cart.emptyFor("alice").add(item(1, "10.00", 2)).add(item(2, "5.50", 1));

        assertThat(cart.total()).isEqualTo(Money.of("25.50"));
    }

    @Test
    void remove_dropsOnlyThatProduct() {
        Cart cart = Cart.emptyFor("alice").add(item(1, "10.00", 1)).add(item(2, "5.00", 1)).remove(1);

        assertThat(cart.items()).extracting(CartItem::productId).containsExactly(2L);
    }

    @Test
    void items_listIsUnmodifiable() {
        Cart cart = Cart.emptyFor("alice").add(item(1, "10.00", 1));

        assertThatThrownBy(() -> cart.items().add(item(2, "1.00", 1)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void item_rejectsInvalidQuantity() {
        assertThatThrownBy(() -> item(1, "10.00", 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> item(1, "10.00", 100)).isInstanceOf(IllegalArgumentException.class);
    }
}
