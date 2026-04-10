package com.s2p.FCT.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.s2p.FCT.entity.Customers;

public interface CustomersRepository extends JpaRepository<Customers, UUID>{
	
	 Customers findByEmail(String email);

    @Query("SELECT c FROM Customers c LEFT JOIN FETCH c.addresses WHERE c.email = :email")
    Optional<Customers> findByEmailWithAddresses(@Param("email") String email);


}
