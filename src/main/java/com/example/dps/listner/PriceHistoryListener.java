package com.example.dps.listner;

import com.example.dps.record.PriceChangeEvent;
import com.example.dps.service.PriceHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PriceHistoryListener {
    private final PriceHistoryService priceHistoryService;
    @Async
    @EventListener
    public void onPriceChange(PriceChangeEvent event) {
        log.info("Price changed for product ID: {} from {} to {} triggered by: {}", event.productId(), event.oldPrice(), event.newPrice(), event.triggeredBy());
        priceHistoryService.recordPriceChange(event.productId(), event.newPrice(), event.triggeredBy());
    }
}
