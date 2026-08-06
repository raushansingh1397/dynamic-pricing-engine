package com.example.dps.repository;

import com.example.dps.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;



@Repository
public interface ProductRepo extends JpaRepository<Product, Integer> {
    @Query(value = "SELECT p FROM Product p where p.isActive=true")
    Page<Product> findAllActiveProducts(Pageable pageable);
}
