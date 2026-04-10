package com.s2p.FCT.controllers;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.s2p.FCT.model.requestModel.OrderRequest;
import com.s2p.FCT.services.OrderService;
import com.s2p.FCT.services.Impl.OrderStatus;

@RestController
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // Create Order
    @PostMapping("/save")
    public ResponseEntity<Object> saveOrder(@RequestBody OrderRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderService.createOrder(request));
    }

    // Get All Orders
    @GetMapping("/getall")
    public ResponseEntity<Object> getAllOrders() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(orderService.findAllOrders());
    }

    // Get Orders By Customer
    @GetMapping("/{customerId}")
    public ResponseEntity<Object> getAllOrdersByCustomer(@PathVariable UUID customerId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(orderService.findByCustomerId(customerId));
    }

    // Get Orders By Status
    @GetMapping("/status/{status}")
    public ResponseEntity<Object> getOrdersByStatus(@PathVariable OrderStatus status) {
        return ResponseEntity.ok(orderService.findByStatus(status));
    }

    // Get Delivered Orders By Date
    @GetMapping("/delivered/date")
    public ResponseEntity<Object> getDeliveredOrdersByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return ResponseEntity.ok(orderService.findDeliveredByDate(date));
    }

    // Update Order Status
    @PatchMapping("/update-status/{orderId}")
    public ResponseEntity<Object> updateOrderStatus(
            @PathVariable UUID orderId,
            @RequestParam OrderStatus status) {

        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status));
    }

    @PatchMapping("/{orderId}/ship")
    public ResponseEntity<Object> markAsShipped(@PathVariable UUID orderId) {
        return ResponseEntity.ok(
                orderService.updateOrderStatus(orderId, OrderStatus.SHIPPED));
    }

    @PatchMapping("/{orderId}/deliver")
    public ResponseEntity<Object> markAsDelivered(@PathVariable UUID orderId) {
        return ResponseEntity.ok(
                orderService.updateOrderStatus(orderId, OrderStatus.DELIVERED));
    }
}