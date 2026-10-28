package com.example.shop.order;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {

    List<OrderEntity> findByOwnerOrderByPlacedAtDesc(String owner);

    Optional<OrderEntity> findByIdAndOwner(Long id, String owner);
}
