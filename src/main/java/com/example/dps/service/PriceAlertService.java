package com.example.dps.service;

import com.example.dps.entity.PriceAlert;
import com.example.dps.entity.Product;
import com.example.dps.repository.PriceAlertRepo;
import com.example.dps.repository.ProductRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PriceAlertService {
    private final PriceAlertRepo priceAlertRepo;
    private final ProductRepo productRepo;

    public void createPriceAlert(Integer productId, BigDecimal newPrice, BigDecimal oldPrice, BigDecimal basePrice, String alertType, String triggeredBy) {
        PriceAlert priceAlert = new PriceAlert();
        Product product = productRepo.getReferenceById(productId);
        priceAlert.setProduct(product);
        priceAlert.setOldPrice(oldPrice);
        priceAlert.setNewPrice(newPrice);
        priceAlert.setBasePrice(basePrice);
        priceAlert.setAlertType(alertType);
        priceAlert.setTriggeredBy(triggeredBy);
        priceAlertRepo.save(priceAlert);
    }
}
