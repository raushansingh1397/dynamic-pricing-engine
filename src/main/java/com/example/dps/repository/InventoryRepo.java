package com.example.dps.repository;

import com.example.dps.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InventoryRepo extends JpaRepository<Inventory, Integer> {
    @Query("SELECT i FROM Inventory i where i.prodId=:prodId")
    Optional<Inventory> getProductById(@Param("prodId") int prodId);
}
