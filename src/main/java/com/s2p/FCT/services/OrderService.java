package com.s2p.FCT.services;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.s2p.FCT.model.requestModel.OrderRequest;
import com.s2p.FCT.services.Impl.OrderStatus;

public interface OrderService {

    String createOrder(OrderRequest order);

    List<OrderRequest> findAllOrders();

    List<OrderRequest> findByCustomerId(UUID customerId);

    List<OrderRequest> findByStatus(OrderStatus status);

    List<OrderRequest> findDeliveredByDate(LocalDate date);

    String updateOrderStatus(UUID orderId, OrderStatus status);
}