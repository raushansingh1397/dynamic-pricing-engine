package com.example.dps.repository;

import com.example.dps.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepo extends JpaRepository<Product, Integer> {
    @Query(value = "SELECT * FROM Product p where p.is_active=true", nativeQuery = true)
    List<Product> findAllActiveProducts();
}
