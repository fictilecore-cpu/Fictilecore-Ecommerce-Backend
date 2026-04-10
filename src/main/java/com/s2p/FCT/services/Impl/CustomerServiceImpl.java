package com.s2p.FCT.services.Impl;

import com.s2p.FCT.entity.Customers;
import com.s2p.FCT.model.AuthResponse;
import com.s2p.FCT.model.CustomerAddress;
import com.s2p.FCT.model.CustomerModel;
import com.s2p.FCT.repositories.CustomersRepository;
import com.s2p.FCT.security.JwtUtil;
import com.s2p.FCT.services.CustomerService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomersRepository customersRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public CustomerServiceImpl(CustomersRepository customersRepository,
                               PasswordEncoder passwordEncoder,
                               JwtUtil jwtUtil) {
        this.customersRepository = customersRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // ================= CREATE =================
    @Override
    public Customers createCustomer(Customers customer) {
        if (customersRepository.findByEmail(customer.getEmail()) != null) {
            throw new RuntimeException("Email already registered");
        }

        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        customer.setRole("USER");

        if (customer.getAddresses() != null) {
            customer.getAddresses().forEach(addr -> addr.setCustomer(customer));
        }

        return customersRepository.save(customer);
    }

    // ================= LOGIN =================
    @Override
    @Transactional(readOnly = true)
    public AuthResponse logIn(String email, String password) {
        Customers customer = customersRepository.findByEmailWithAddresses(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(password, customer.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(customer.getEmail(), customer.getRole());
        return toAuthResponse(customer, token);
    }

    // ================= READ =================
    @Override
    @Transactional(readOnly = true)
    public CustomerModel getCustomerById(UUID id) {
        Customers customer = customersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return toCustomerModel(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerModel> getAllCustomers() {
        return customersRepository.findAll()
                .stream()
                .map(this::toCustomerModel)
                .collect(Collectors.toList());
    }

    // ================= DTO MAPPER =================
    @Override
    public CustomerModel toCustomerModel(Customers customer) {
        CustomerModel model = new CustomerModel();
        model.setId(customer.getId());
        model.setFirstName(customer.getFirstName());
        model.setLastName(customer.getLastName());
        model.setEmail(customer.getEmail());
        model.setPhoneNumber(customer.getPhoneNumber());
        model.setRole(customer.getRole());

        if (customer.getAddresses() != null) {
            List<CustomerAddress> safeAddresses = customer.getAddresses().stream()
                    .map(addr -> {
                        CustomerAddress safeAddr = new CustomerAddress();
                        safeAddr.setId(addr.getId());
                        safeAddr.setStreet(addr.getStreet());
                        safeAddr.setCity(addr.getCity());
                        safeAddr.setState(addr.getState());
                        safeAddr.setPincode(addr.getPincode());
                        return safeAddr;
                    }).collect(Collectors.toList());
            model.setAddresses(safeAddresses);
        }

        return model;
    }

    // ================= NEW DTO FOR JWT =================
    @Override
    public AuthResponse toAuthResponse(Customers customer, String token) {
        AuthResponse response = new AuthResponse();
        response.setId(customer.getId());
        response.setFirstName(customer.getFirstName());
        response.setLastName(customer.getLastName());
        response.setEmail(customer.getEmail());
        response.setPhoneNumber(customer.getPhoneNumber());
        response.setRole(customer.getRole());
        response.setToken(token);

        if (customer.getAddresses() != null) {
            List<CustomerAddress> safeAddresses = customer.getAddresses().stream()
                    .map(addr -> {
                        CustomerAddress safeAddr = new CustomerAddress();
                        safeAddr.setId(addr.getId());
                        safeAddr.setStreet(addr.getStreet());
                        safeAddr.setCity(addr.getCity());
                        safeAddr.setState(addr.getState());
                        safeAddr.setPincode(addr.getPincode());
                        return safeAddr;
                    }).collect(Collectors.toList());
            response.setAddresses(safeAddresses);
        }

        return response;
    }

    // ================= UPDATE =================
    @Override
    public Customers updateCustomer(UUID id, Customers updated) {
        Customers existing = customersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setEmail(updated.getEmail());
        existing.setPhoneNumber(updated.getPhoneNumber());

        return customersRepository.save(existing);
    }

    @Override
    public Customers updateCustomerAddress(UUID id, Customers updated) {
        Customers existing = customersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (updated.getAddresses() != null) {
            updated.getAddresses().forEach(addr -> addr.setCustomer(existing));
            existing.setAddresses(updated.getAddresses());
        }

        return customersRepository.save(existing);
    }

    // ================= DELETE =================
    @Override
    public void deleteCustomer(UUID id) {
        customersRepository.deleteById(id);
    }

    @Override
    public Customers getCustomerByEmail(String email) {
        return customersRepository.findByEmailWithAddresses(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }
}