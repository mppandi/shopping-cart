package com.example.shop.product;

import java.util.List;
import java.util.Optional;

/** PORT: what the application needs from "somewhere that stores products". */
public interface ProductCatalog {

    List<Product> findAll();

    Optional<Product> findById(long id);
}
