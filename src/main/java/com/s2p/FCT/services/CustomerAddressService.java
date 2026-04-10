package com.s2p.FCT.services;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.s2p.FCT.entity.Customers;
import com.s2p.FCT.model.CustomerAddress;
// import com.s2p.FCT.model.dto.CustomersDTO;
import com.s2p.FCT.repositories.CustomerAddressRepo;
import com.s2p.FCT.repositories.CustomersRepository;


@Service
public class CustomerAddressService {

    @Autowired
    private CustomersRepository customersRepository;

    @Autowired
    private CustomerAddressRepo addressRepository;

    public CustomerAddress saveAddress(UUID customerId, CustomerAddress address) {
        Customers customer = customersRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        address.setCustomer(customer); // link the address to the customer
        return addressRepository.save(address); // return the saved address
    }

    // public static CustomerAddress getCustomerById(UUID id) {
    //     // TODO Auto-generated method stub
    //     throw new UnsupportedOperationException("Unimplemented method 'getCustomerById'");
    // }
}









