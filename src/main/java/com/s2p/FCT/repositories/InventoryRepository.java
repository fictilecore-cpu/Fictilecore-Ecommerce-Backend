package com.s2p.FCT.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import com.s2p.FCT.entity.Inventory;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, UUID>{


     @Query("SELECT i FROM Inventory i WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Inventory> searchByName(@Param("keyword") String keyword);

    // Optional: advanced multi-keyword search
    @Query("SELECT i FROM Inventory i WHERE " +
           "LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword1, '%')) " +
           "OR LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword2, '%'))")
    List<Inventory> searchByMultipleKeywords(@Param("keyword1") String keyword1,
                                             @Param("keyword2") String keyword2);

}
