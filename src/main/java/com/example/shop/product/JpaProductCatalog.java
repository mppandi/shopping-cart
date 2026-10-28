package com.example.shop.product;

import com.example.shop.common.Money;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** ADAPTER: implements the ProductCatalog port with Spring Data JPA and maps entity to domain. */
@Repository
class JpaProductCatalog implements ProductCatalog {

    private final ProductJpaRepository jpa;

    JpaProductCatalog(ProductJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Product> findAll() {
        return jpa.findAll().stream().map(JpaProductCatalog::toDomain).toList();
    }

    @Override
    public Optional<Product> findById(long id) {
        return jpa.findById(id).map(JpaProductCatalog::toDomain);
    }

    private static Product toDomain(ProductEntity e) {
        return new Product(e.id, e.name, new Money(e.price));
    }
}
