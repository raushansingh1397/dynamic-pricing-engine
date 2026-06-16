package com.example.dps.listner;

import com.example.dps.record.PriceChangeEvent;
import com.example.dps.service.PriceAlertService;
import com.example.dps.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class PriceAlertListener {
    private final PriceAlertService priceAlertService;
    @Value("${price.alert.threshold:0.10}") // Default threshold value if not set in application.properties
    private String threshold;

    @Async
    @EventListener
    public void checkThreshold(PriceChangeEvent event) {
        double oldPrice = event.oldPrice().doubleValue();
        double newPrice = event.newPrice().doubleValue();
        double priceChangePercentage = Math.abs((newPrice - oldPrice) / oldPrice);

        if (priceChangePercentage > Double.parseDouble(threshold)) {
            // Trigger alert logic here
            log.info("Alert: Price change for product ID {} exceeds threshold. Old Price: {}, New Price: {}", event.productId(), oldPrice, newPrice);
            // store the alert in the database
            priceAlertService.createPriceAlert(event.productId(), event.newPrice(), event.oldPrice(),event.basePrice(), Constants.PRICE_INCREASE, event.triggeredBy());
        }
    }
}
