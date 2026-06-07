package com.example.dps.repository;

import com.example.dps.entity.CompPricing;
import com.example.dps.entity.CompPricingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompPricingRepo extends JpaRepository<CompPricing, CompPricingId> {
    List<CompPricing> findByProductProdId(Integer prodId);
}
