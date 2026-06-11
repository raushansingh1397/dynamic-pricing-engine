package com.example.dps.service;

import com.example.dps.entity.CompPricing;
import com.example.dps.entity.CompPricingId;
import com.example.dps.entity.PriceHistory;
import com.example.dps.entity.Product;
import com.example.dps.exception.ConflictException;
import com.example.dps.exception.ResourceNotFoundException;
import com.example.dps.repository.PriceHistoryRepo;
import com.example.dps.repository.ProductRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceHistoryServiceTest {
    @Mock
    private ProductRepo productRepo;

    @Mock
    private PriceHistoryRepo priceHistoryRepo;

    @InjectMocks
    private PriceHistoryService priceHistoryService;

    @Mock
    private CompPricingService compPricingService;

    @Mock
    private Product product;


    @Test
    void recordPriceChange_ShouldRecordPriceChange() {
        // Given
        int prodId = 1;
        BigDecimal newPrice = new BigDecimal("100.00");
        String triggeredBy = "Admin";

        CompPricingId id1 = new CompPricingId(1, 1);
        CompPricing pricing1 = new CompPricing();
        pricing1.setId(id1);
        pricing1.setPrice(new BigDecimal("899.99"));
        pricing1.setFetchedAt(LocalDateTime.now());

        CompPricingId id2 = new CompPricingId(1, 2);
        CompPricing pricing2 = new CompPricing();
        pricing2.setId(id2);
        pricing2.setPrice(new BigDecimal("850.00"));
        pricing2.setFetchedAt(LocalDateTime.now());

        when(productRepo.findById(prodId)).thenReturn(Optional.of(product));
        when(product.getBasePrice()).thenReturn(new BigDecimal("120.00"));
        when(product.getDiscountedPrice()).thenReturn(new BigDecimal("90.00"));
        when(compPricingService.findAllCompPrices(prodId)).thenReturn(List.of(pricing1, pricing2));


        // When
        priceHistoryService.recordPriceChange(prodId, new BigDecimal("100.00"), "Admin");

        // Then
        // Verify that the repo's save method was called with a PriceHistory object
        verify(priceHistoryRepo).save(any(PriceHistory.class));
    }

    @Test
    void recordPriceChange_ShouldThrowResourceNotFoundException() {
        // Given
        int prodId = 1;
        BigDecimal newPrice = new BigDecimal("100.00");
        String triggeredBy = "Admin";

        when(productRepo.findById(prodId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            priceHistoryService.recordPriceChange(prodId, newPrice, triggeredBy);
        });
    }

    @Test
    void getHistory_ShouldReturnPriceHistoryList() {
        // Given
        int prodId = 1;

        PriceHistory history1 = new PriceHistory();
        history1.setHistoryId(1);
        history1.setBasePrice(new BigDecimal("120.00"));
        history1.setDiscountedPrice(new BigDecimal("90.00"));
        history1.setCompPrice(new BigDecimal("875.00"));
        history1.setTriggeredBy("Admin");
        history1.setCalculatedAt(LocalDateTime.now());

        PriceHistory history2 = new PriceHistory();
        history2.setHistoryId(2);
        history2.setBasePrice(new BigDecimal("110.00"));
        history2.setDiscountedPrice(new BigDecimal("80.00"));
        history2.setCompPrice(new BigDecimal("860.00"));
        history2.setTriggeredBy("Admin");
        history2.setCalculatedAt(LocalDateTime.now());

        when(productRepo.findById(prodId)).thenReturn(Optional.of(product));
        when(product.getIsActive()).thenReturn(true);
        when(priceHistoryRepo.findByProductProdId(prodId)).thenReturn(List.of(history1, history2));

        // When
        List<PriceHistory> priceHistories = priceHistoryService.getHistory(prodId);

        // Then
        assertNotNull(priceHistories);
        assertEquals(2, priceHistories.size());
    }

    @Test
    void getHistory_ShouldThrowResourceNotFoundException() {
        // Given
        int prodId = 1;

        when(productRepo.findById(prodId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            priceHistoryService.getHistory(prodId);
        });
    }

    @Test
    void getHistory_ShouldThrowConflictException() {
        // Given
        int prodId = 1;

        when(productRepo.findById(prodId)).thenReturn(Optional.of(product));
        when(product.getIsActive()).thenReturn(false);

        // When & Then
        assertThrows(ConflictException.class, () -> {
            priceHistoryService.getHistory(prodId);
        });
    }

}
