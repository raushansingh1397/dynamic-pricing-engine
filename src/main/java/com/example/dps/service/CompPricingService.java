package com.example.dps.service;

import com.example.dps.entity.CompPricing;
import com.example.dps.repository.CompPricingRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompPricingService {
    private final CompPricingRepo repo;

    @Autowired
    public CompPricingService(CompPricingRepo repo) {
        this.repo = repo;
    }

    public List<CompPricing> findAllCompPrices(Integer prodId){
        return repo.findByProductProdId(prodId);
    }

}
