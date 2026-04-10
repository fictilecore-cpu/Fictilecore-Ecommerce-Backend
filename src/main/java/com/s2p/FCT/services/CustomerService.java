package com.s2p.FCT.services;

import java.util.List;
import java.util.UUID;

import com.s2p.FCT.entity.Customers;
import com.s2p.FCT.model.AuthResponse;
import com.s2p.FCT.model.CustomerModel;

public interface CustomerService {

    // ------------------- Customer CRUD -------------------
    Customers createCustomer(Customers customers);
    CustomerModel getCustomerById(UUID id);
    List<CustomerModel> getAllCustomers();
    Customers updateCustomer(UUID id, Customers customers);
    void deleteCustomer(UUID id);
    Customers updateCustomerAddress(UUID id, Customers updatedCustomer);

    // ------------------- Authentication -------------------
    AuthResponse logIn(String email, String password);
    Customers getCustomerByEmail(String email);

    // ------------------- DTO Mapping -------------------
    CustomerModel toCustomerModel(Customers customer);

    /**
     * Convert Customers entity to AuthResponse DTO including token and addresses.
     * This method ensures lazy-loaded collections are initialized safely.
     */
    AuthResponse toAuthResponse(Customers customer, String token);

}