package com.example.shop.order;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Persistence model only. "orders" because ORDER is a reserved word in SQL. */
@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String owner;

    @Column(nullable = false, precision = 19, scale = 2)
    BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    OrderStatus status;

    @Column(nullable = false)
    Instant placedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_lines", joinColumns = @JoinColumn(name = "order_id"))
    List<OrderLineRow> lines = new ArrayList<>();

    protected OrderEntity() {
        // required by JPA
    }

    OrderEntity(Long id, String owner, BigDecimal total, OrderStatus status, Instant placedAt, List<OrderLineRow> lines) {
        this.id = id;
        this.owner = owner;
        this.total = total;
        this.status = status;
        this.placedAt = placedAt;
        this.lines = new ArrayList<>(lines);
    }
}
