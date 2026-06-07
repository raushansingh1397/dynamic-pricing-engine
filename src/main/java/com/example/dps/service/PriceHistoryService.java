package com.example.dps.service;


import com.example.dps.entity.CompPricing;
import com.example.dps.entity.PriceHistory;
import com.example.dps.entity.Product;
import com.example.dps.exception.ConflictException;
import com.example.dps.exception.ResourceNotFoundException;
import com.example.dps.repository.PriceHistoryRepo;
import com.example.dps.repository.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Service
public class PriceHistoryService {
    private final ProductRepo productRepo;
    private final PriceHistoryRepo repo;
    private final CompPricingService compService;

    @Autowired
    public PriceHistoryService(ProductRepo productRepo, PriceHistoryRepo repo, CompPricingService compService) {
        this.productRepo = productRepo;
        this.repo = repo;
        this.compService = compService;
    }

    public void recordPriceChange(int prodId, BigDecimal newPrice, String triggeredBy){
         PriceHistory priceHistory = new PriceHistory();
         Product product = productRepo.findById(prodId).orElseThrow(()->new ResourceNotFoundException("No Such Product !"));
         priceHistory.setBasePrice(product.getBasePrice());
         priceHistory.setTriggeredBy(triggeredBy);
         priceHistory.setDiscountedPrice(product.getDiscountedPrice());
         BigDecimal avgPrice = avgCompPrice(compService.findAllCompPrices(prodId));
         priceHistory.setCompPrice(avgPrice);
         priceHistory.setCalculatedAt(LocalDateTime.now());
         priceHistory.setProduct(product);
         repo.save(priceHistory);

    }

    public List<PriceHistory> getHistory(int prodId){
        Product product = productRepo.findById(prodId).orElseThrow(()->new ResourceNotFoundException("Product not found"));
        if(!product.getIsActive()) throw  new ConflictException("Product is inactive");
        return repo.findByProductProdId(prodId);
    }

    private BigDecimal avgCompPrice(List<CompPricing> list){
        double avg = list.stream().map(CompPricing::getPrice)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0);
        return BigDecimal.valueOf(avg);
    }
}
