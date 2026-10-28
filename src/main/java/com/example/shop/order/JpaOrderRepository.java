package com.example.shop.order;

import com.example.shop.common.Money;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** ADAPTER for the OrderRepository port. */
@Repository
class JpaOrderRepository implements OrderRepository {

    private final OrderJpaRepository jpa;

    JpaOrderRepository(OrderJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Order save(Order order) {
        return toDomain(jpa.save(toEntity(order)));
    }

    @Override
    public Optional<Order> findByIdAndOwner(long id, String owner) {
        return jpa.findByIdAndOwner(id, owner).map(JpaOrderRepository::toDomain);
    }

    @Override
    public List<Order> findByOwner(String owner) {
        return jpa.findByOwnerOrderByPlacedAtDesc(owner).stream().map(JpaOrderRepository::toDomain).toList();
    }

    private static Order toDomain(OrderEntity e) {
        return new Order(
                e.id,
                e.owner,
                e.lines.stream()
                        .map(r -> new OrderLine(r.productId(), r.name(), new Money(r.unitPrice()), r.quantity()))
                        .toList(),
                new Money(e.total),
                e.status,
                e.placedAt);
    }

    private static OrderEntity toEntity(Order o) {
        return new OrderEntity(
                o.id(),
                o.owner(),
                o.total().amount(),
                o.status(),
                o.placedAt(),
                o.lines().stream()
                        .map(l -> new OrderLineRow(l.productId(), l.name(), l.unitPrice().amount(), l.quantity()))
                        .toList());
    }
}
