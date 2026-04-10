package com.s2p.FCT.services.Impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.s2p.FCT.entity.Customers;
import com.s2p.FCT.entity.Inventory;
import com.s2p.FCT.entity.Order;
import com.s2p.FCT.entity.OrderItem;
import com.s2p.FCT.model.CustomerAddress;
import com.s2p.FCT.model.requestModel.OrderItemRequest;
import com.s2p.FCT.model.requestModel.OrderRequest;
import com.s2p.FCT.repositories.CustomerAddressRepo;
import com.s2p.FCT.repositories.CustomersRepository;
import com.s2p.FCT.repositories.InventoryRepository;
import com.s2p.FCT.repositories.OrderRepository;
import com.s2p.FCT.services.OrderService;

import jakarta.transaction.Transactional;

@Service
public class OrderServiceImpl implements OrderService {

    private final CustomersRepository customerRepository;
    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;
    private final CustomerAddressRepo customerAddressRepo;

    @Autowired
    public OrderServiceImpl(
            CustomersRepository customerRepository,
            OrderRepository orderRepository,
            InventoryRepository inventoryRepository,
            CustomerAddressRepo customerAddressRepo) {

        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.inventoryRepository = inventoryRepository;
        this.customerAddressRepo = customerAddressRepo;
    }

    // CREATE ORDER
    @Override
    @Transactional
    public String createOrder(OrderRequest orderRequest) {

        Optional<Customers> customerOpt = customerRepository.findById(orderRequest.getCustomerId());
        Optional<CustomerAddress> addressOpt = customerAddressRepo.findById(orderRequest.getAddressId());

        if (customerOpt.isEmpty()) {
            return "Customer not found";
        }

        if (addressOpt.isEmpty()) {
            return "Address not available";
        }

        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setOrderDate(LocalDateTime.now());
        order.setCustomer(customerOpt.get());
        order.setCustomerAddress(addressOpt.get());
        order.setStatus(OrderStatus.ORDERED);
        order.setTotalAmount(orderRequest.getTotalAmount());

        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest item : orderRequest.getItems()) {

            Optional<Inventory> inventoryOpt = inventoryRepository.findById(item.getProductId());

            if (inventoryOpt.isEmpty()) {
                return "Product not found: " + item.getProductId();
            }

            Inventory inventory = inventoryOpt.get();

            // Check stock availability
            if (inventory.getQuantityAvailable() < item.getQuantity()) {
                return "Insufficient stock for product: " + inventory.getName();
            }

            // Reduce stock
            inventory.setQuantityAvailable(inventory.getQuantityAvailable() - item.getQuantity());
            inventoryRepository.save(inventory);

            OrderItem orderItem = new OrderItem();
            orderItem.setId(UUID.randomUUID());
            orderItem.setInventory(inventory);
            orderItem.setQuantity(item.getQuantity());
            orderItem.setOrder(order);

            orderItems.add(orderItem);
        }

        order.setOrderItems(orderItems);

        orderRepository.save(order);

        return "Order created successfully";
    }

    // GET ALL ORDERS
    @Override
    @Transactional
    public List<OrderRequest> findAllOrders() {

        List<Order> orders = orderRepository.findByStatus(OrderStatus.ORDERED);
        return convertToOrderRequestList(orders);
    }




    // GET ORDERS BY CUSTOMER
    @Override
    @Transactional
    public List<OrderRequest> findByCustomerId(UUID customerId) {

        List<Order> orders = orderRepository.findByCustomer_Id(customerId);
        return convertToOrderRequestList(orders);
    }

    // GET ORDERS BY STATUS
    @Override
    @Transactional
    public List<OrderRequest> findByStatus(OrderStatus status) {

        List<Order> orders = orderRepository.findByStatus(status);
        return convertToOrderRequestList(orders);
    }

    // GET DELIVERED ORDERS BY DATE
    @Override
    @Transactional
    public List<OrderRequest> findDeliveredByDate(LocalDate date) {

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<Order> orders =
                orderRepository.findByStatusAndOrderDateBetween(OrderStatus.DELIVERED, start, end);

        return convertToOrderRequestList(orders);
    }

    // UPDATE ORDER STATUS
 @Override
@Transactional
public String updateOrderStatus(UUID orderId, OrderStatus status) {

    Optional<Order> orderOpt = orderRepository.findById(orderId);

    if (orderOpt.isEmpty()) {
        return "Order not found";
    }

    List<String> validStatus = List.of("PENDING", "SHIPPING", "DELIVERED");



    if (!validStatus.contains(status)) {
        return "Invalid order status";
    }

    Order order = orderOpt.get();

    order.setStatus(status);

    if (status.equals("SHIPPING")) {
        order.setShippingDate(LocalDateTime.now());
    }

    if (status.equals("DELIVERED")) {
        order.setDeliveredDate(LocalDateTime.now());
    }

    orderRepository.save(order);

    return "Order status updated successfully";
}
    // COMMON MAPPING METHOD
    private List<OrderRequest> convertToOrderRequestList(List<Order> orders) {

        List<OrderRequest> orderRequests = new ArrayList<>();

        for (Order order : orders) {

            OrderRequest orderRequest = new OrderRequest();
            orderRequest.setId(order.getId());
            orderRequest.setCustomerId(order.getCustomer().getId());
            orderRequest.setCustomerName(
                    order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName());

            orderRequest.setEmail(order.getCustomer().getEmail());
            orderRequest.setOrderDate(order.getOrderDate().toLocalDate());
            orderRequest.setTotalAmount(order.getTotalAmount());

            if (order.getCustomerAddress() != null) {
                orderRequest.setAddressId(order.getCustomerAddress().getId());
            }

            List<OrderItemRequest> itemRequests = new ArrayList<>();

            for (OrderItem item : order.getOrderItems()) {

                OrderItemRequest itemRequest = new OrderItemRequest();
                itemRequest.setProductId(item.getInventory().getId());
                itemRequest.setProductName(item.getInventory().getName());
                itemRequest.setQuantity(item.getQuantity());

                itemRequests.add(itemRequest);
            }

            orderRequest.setItems(itemRequests);

            orderRequests.add(orderRequest);
        }

        return orderRequests;
    }
}