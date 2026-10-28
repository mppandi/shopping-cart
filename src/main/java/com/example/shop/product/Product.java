package com.example.shop.product;

import com.example.shop.common.Money;

/** Immutable domain object (no JPA annotations). */
public record Product(long id, String name, Money price) {}
