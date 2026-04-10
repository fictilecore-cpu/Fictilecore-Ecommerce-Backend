package com.s2p.FCT.model.requestModel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderRequest {
    private UUID id;
    private String customerName;
    private UUID customerId;
    private String email;
    private LocalDate orderDate;
    private List<OrderItemRequest> items;
    private BigDecimal totalAmount;
    private UUID addressId;  // <-- Add this field

 
}