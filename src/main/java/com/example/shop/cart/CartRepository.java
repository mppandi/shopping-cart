package com.example.shop.cart;

import java.util.Optional;

/** PORT (Dependency Inversion): services depend on this interface, never on JPA. */
public interface CartRepository {

    Optional<Cart> findByOwner(String owner);

    Cart save(Cart cart);
}
