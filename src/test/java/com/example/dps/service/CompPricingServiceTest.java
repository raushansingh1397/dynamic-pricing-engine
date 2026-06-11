package com.example.dps.service;

import com.example.dps.entity.CompPricing;
import com.example.dps.entity.CompPricingId;
import com.example.dps.repository.CompPricingRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompPricingServiceTest {
    @Mock
    private CompPricingRepo compPricingRepo;
    @InjectMocks
    private CompPricingService compPricingService;
    @Test
    void testFindAllCompPrices() {
        // Given
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

        when(compPricingRepo.findByProductProdId(1)).thenReturn(List.of(pricing1,pricing2));
        List<CompPricing> compPrices = compPricingService.findAllCompPrices(1);

        // assertion to check if the returned list is not null and has the expected size
        assertNotNull(compPrices);
        assertEquals(2, compPrices.size());
        assertEquals(new BigDecimal("899.99"), compPrices.get(0).getPrice());
        assertEquals(new BigDecimal("850.00"), compPrices.get(1).getPrice());
    }
}
