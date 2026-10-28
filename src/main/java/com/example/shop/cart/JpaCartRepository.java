package com.example.shop.cart;

import com.example.shop.common.Money;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** ADAPTER: implements the CartRepository port and maps between the entity and the immutable domain. */
@Repository
class JpaCartRepository implements CartRepository {

    private final CartJpaRepository jpa;

    JpaCartRepository(CartJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Cart> findByOwner(String owner) {
        return jpa.findById(owner).map(JpaCartRepository::toDomain);
    }

    @Override
    public Cart save(Cart cart) {
        jpa.save(toEntity(cart));
        return cart;
    }

    private static Cart toDomain(CartEntity e) {
        return new Cart(
                e.owner,
                e.items.stream()
                        .map(r -> new CartItem(r.productId(), r.name(), new Money(r.unitPrice()), r.quantity()))
                        .toList());
    }

    private static CartEntity toEntity(Cart cart) {
        return new CartEntity(
                cart.owner(),
                cart.items().stream()
                        .map(i -> new CartItemRow(i.productId(), i.name(), i.unitPrice().amount(), i.quantity()))
                        .toList());
    }
}
