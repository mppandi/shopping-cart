package com.example.shop.order;

import java.util.List;
import java.util.Optional;

/** PORT. */
public interface OrderRepository {

    /** Saves a new order (id is assigned) or updates an existing one. Returns the saved order. */
    Order save(Order order);

    Optional<Order> findByIdAndOwner(long id, String owner);

    List<Order> findByOwner(String owner);
}
