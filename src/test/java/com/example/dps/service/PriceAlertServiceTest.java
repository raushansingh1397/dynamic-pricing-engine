package com.example.dps.service;

import com.example.dps.entity.PriceAlert;
import com.example.dps.entity.Product;
import com.example.dps.repository.PriceAlertRepo;
import com.example.dps.repository.ProductRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceAlertServiceTest {

    @Mock
    private PriceAlertRepo priceAlertRepo;

    @Mock
    private ProductRepo productRepo;

    @InjectMocks
    private PriceAlertService priceAlertService;

    @Test
    void testCreatePriceAlert_success() {
        // Arrange
        Integer productId = 1;
        BigDecimal oldPrice = new BigDecimal("100.00");
        BigDecimal newPrice = new BigDecimal("110.00");
        BigDecimal basePrice = new BigDecimal("100.00");
        String alertType = "INCREASE";
        String triggeredBy = "COMPETITOR";

        Product product = new Product();
        product.setProdId(productId);
        product.setProdName("Test Product");

        when(productRepo.getReferenceById(productId)).thenReturn(product);
        when(priceAlertRepo.save(any(PriceAlert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        priceAlertService.createPriceAlert(productId, newPrice, oldPrice, basePrice, alertType, triggeredBy);

        // Assert
        ArgumentCaptor<PriceAlert> alertCaptor = ArgumentCaptor.forClass(PriceAlert.class);
        verify(priceAlertRepo, times(1)).save(alertCaptor.capture());
        verify(productRepo, times(1)).getReferenceById(productId);

        PriceAlert savedAlert = alertCaptor.getValue();
        assertEquals(product, savedAlert.getProduct());
        assertEquals(oldPrice, savedAlert.getOldPrice());
        assertEquals(newPrice, savedAlert.getNewPrice());
        assertEquals(basePrice, savedAlert.getBasePrice());
        assertEquals("INCREASE", savedAlert.getAlertType());
        assertEquals("COMPETITOR", savedAlert.getTriggeredBy());
    }

    @Test
    void testCreatePriceAlert_priceDecrease() {
        // Arrange
        Integer productId = 2;
        BigDecimal oldPrice = new BigDecimal("150.00");
        BigDecimal newPrice = new BigDecimal("120.00");
        BigDecimal basePrice = new BigDecimal("150.00");
        String alertType = "DECREASE";
        String triggeredBy = "DYNAMIC_PRICING";

        Product product = new Product();
        product.setProdId(productId);

        when(productRepo.getReferenceById(productId)).thenReturn(product);
        when(priceAlertRepo.save(any(PriceAlert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        priceAlertService.createPriceAlert(productId, newPrice, oldPrice, basePrice, alertType, triggeredBy);

        // Assert
        ArgumentCaptor<PriceAlert> alertCaptor = ArgumentCaptor.forClass(PriceAlert.class);
        verify(priceAlertRepo, times(1)).save(alertCaptor.capture());

        PriceAlert savedAlert = alertCaptor.getValue();
        assertEquals("DECREASE", savedAlert.getAlertType());
        assertEquals("DYNAMIC_PRICING", savedAlert.getTriggeredBy());
        assertEquals(newPrice, savedAlert.getNewPrice());
        assertTrue(savedAlert.getNewPrice().compareTo(savedAlert.getOldPrice()) < 0);
    }

    @Test
    void testCreatePriceAlert_zeroPrice() {
        // Arrange
        Integer productId = 3;
        BigDecimal oldPrice = new BigDecimal("0.00");
        BigDecimal newPrice = new BigDecimal("50.00");
        BigDecimal basePrice = new BigDecimal("0.00");
        String alertType = "INITIALIZATION";
        String triggeredBy = "MANUAL";

        Product product = new Product();
        product.setProdId(productId);

        when(productRepo.getReferenceById(productId)).thenReturn(product);
        when(priceAlertRepo.save(any(PriceAlert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        priceAlertService.createPriceAlert(productId, newPrice, oldPrice, basePrice, alertType, triggeredBy);

        // Assert
        ArgumentCaptor<PriceAlert> alertCaptor = ArgumentCaptor.forClass(PriceAlert.class);
        verify(priceAlertRepo, times(1)).save(alertCaptor.capture());

        PriceAlert savedAlert = alertCaptor.getValue();
        assertEquals(new BigDecimal("0.00"), savedAlert.getOldPrice());
        assertEquals(new BigDecimal("50.00"), savedAlert.getNewPrice());
    }

    @Test
    void testCreatePriceAlert_largePrice() {
        // Arrange
        Integer productId = 4;
        BigDecimal oldPrice = new BigDecimal("9999.99");
        BigDecimal newPrice = new BigDecimal("10999.99");
        BigDecimal basePrice = new BigDecimal("9999.99");
        String alertType = "CRITICAL_INCREASE";
        String triggeredBy = "MARKET";

        Product product = new Product();
        product.setProdId(productId);

        when(productRepo.getReferenceById(productId)).thenReturn(product);
        when(priceAlertRepo.save(any(PriceAlert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        priceAlertService.createPriceAlert(productId, newPrice, oldPrice, basePrice, alertType, triggeredBy);

        // Assert
        ArgumentCaptor<PriceAlert> alertCaptor = ArgumentCaptor.forClass(PriceAlert.class);
        verify(priceAlertRepo, times(1)).save(alertCaptor.capture());

        PriceAlert savedAlert = alertCaptor.getValue();
        assertEquals(new BigDecimal("9999.99"), savedAlert.getOldPrice());
        assertEquals(new BigDecimal("10999.99"), savedAlert.getNewPrice());
    }

    @Test
    void testCreatePriceAlert_verifyProductFetch() {
        // Arrange
        Integer productId = 5;

        Product product = new Product();
        product.setProdId(productId);
        product.setProdName("Laptop");

        when(productRepo.getReferenceById(productId)).thenReturn(product);
        when(priceAlertRepo.save(any(PriceAlert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        priceAlertService.createPriceAlert(productId, 
                new BigDecimal("100"), 
                new BigDecimal("80"), 
                new BigDecimal("100"), 
                "TEST", 
                "TEST_TRIGGER");

        // Assert
        verify(productRepo, times(1)).getReferenceById(productId);
        verify(priceAlertRepo, times(1)).save(any(PriceAlert.class));
    }

    @Test
    void testCreatePriceAlert_multipleAlerts() {
        // Arrange
        Product product = new Product();
        product.setProdId(1);

        when(productRepo.getReferenceById(anyInt())).thenReturn(product);
        when(priceAlertRepo.save(any(PriceAlert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        for (int i = 0; i < 5; i++) {
            priceAlertService.createPriceAlert(1, 
                    new BigDecimal(100 + i), 
                    new BigDecimal(100), 
                    new BigDecimal(100), 
                    "ALERT_" + i, 
                    "TRIGGER_" + i);
        }

        // Assert
        verify(priceAlertRepo, times(5)).save(any(PriceAlert.class));
    }
}

