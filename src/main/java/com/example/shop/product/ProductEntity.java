package com.example.shop.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** Persistence model only. Fields are package-private so only this package can touch them. */
@Entity
@Table(name = "products")
public class ProductEntity {

    @Id
    Long id;

    @Column(nullable = false)
    String name;

    @Column(nullable = false, precision = 19, scale = 2)
    BigDecimal price;

    protected ProductEntity() {
        // required by JPA
    }
}
