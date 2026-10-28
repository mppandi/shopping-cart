package com.example.shop.cart;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/** Persistence model only (JPA needs a mutable class). The domain uses the immutable Cart record instead. */
@Entity
@Table(name = "carts")
public class CartEntity {

    @Id
    String owner;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "cart_items", joinColumns = @JoinColumn(name = "owner"))
    List<CartItemRow> items = new ArrayList<>();

    protected CartEntity() {
        // required by JPA
    }

    CartEntity(String owner, List<CartItemRow> items) {
        this.owner = owner;
        this.items = new ArrayList<>(items);
    }
}
