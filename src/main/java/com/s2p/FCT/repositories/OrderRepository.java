package com.s2p.FCT.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.s2p.FCT.entity.Order;
import com.s2p.FCT.services.Impl.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    // Orders by Customer
    List<Order> findByCustomer_Id(UUID customerId);

    // Orders by Status
    List<Order> findByStatus(OrderStatus status);

    // Orders by Customer + Status
    List<Order> findByCustomer_IdAndStatus(UUID customerId, OrderStatus status);

    // Orders by Status + Date Range
    List<Order> findByStatusAndOrderDateBetween(
            OrderStatus status,
            LocalDateTime start,
            LocalDateTime end
    );

}