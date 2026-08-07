package com.example.dps.service;

import com.example.dps.dto.JobDTO;
import com.example.dps.entity.Product;
import com.example.dps.repository.ProductRepo;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchedulerServiceTest {

    @Mock
    private PricingEngine pricingEngine;

    @Mock
    private ProductService productService;

    @Mock
    private JobLogService jobLogService;

    @Mock
    private ProductRepo productRepo;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private SchedulerService schedulerService;

    @Test
    void testScheduleDynamicPricing_success() {
        // Arrange
        List<Product> products = new ArrayList<>();
        Product product1 = new Product();
        product1.setProdId(1);
        product1.setDiscountedPrice(new BigDecimal("100.00"));
        products.add(product1);

        Page<Product> productPage = new PageImpl<>(products, PageRequest.of(0, 1000), 1);
        when(productRepo.findAllActiveProducts(any(Pageable.class))).thenReturn(productPage);
        when(pricingEngine.calculateDynamicPrice(product1)).thenReturn(new BigDecimal("110.00"));

        // Act
        schedulerService.scheduleDynamicPricing();

        // Assert
        verify(productRepo, times(1)).findAllActiveProducts(any(Pageable.class));
        verify(pricingEngine, times(1)).calculateDynamicPrice(product1);
        verify(productService, times(1)).updateProductPrice(1, new BigDecimal("110.00"));
        verify(jobLogService, times(1)).updateLogs(any(JobDTO.class));
        verify(entityManager, times(1)).flush();
        verify(entityManager, times(1)).clear();
    }

    @Test
    void testScheduleDynamicPricing_noProducts() {
        // Arrange
        Page<Product> emptyPage = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 1000), 0);
        when(productRepo.findAllActiveProducts(any(Pageable.class))).thenReturn(emptyPage);

        // Act
        schedulerService.scheduleDynamicPricing();

        // Assert
        verify(productRepo, times(1)).findAllActiveProducts(any(Pageable.class));
        verify(pricingEngine, never()).calculateDynamicPrice(any(Product.class));
        verify(productService, never()).updateProductPrice(anyInt(), any(BigDecimal.class));
        verify(jobLogService, times(1)).updateLogs(any(JobDTO.class));
    }

    @Test
    void testScheduleDynamicPricing_multiplePages() {
        // Arrange
        List<Product> page1Products = new ArrayList<>();
        Product product1 = new Product();
        product1.setProdId(1);
        page1Products.add(product1);

        List<Product> page2Products = new ArrayList<>();
        Product product2 = new Product();
        product2.setProdId(2);
        page2Products.add(product2);

        // FIX: Total count set to 2000 so page1.hasNext() evaluates to true for pageSize 1000
        Page<Product> page1 = new PageImpl<>(page1Products, PageRequest.of(0, 1000), 2000);
        Page<Product> page2 = new PageImpl<>(page2Products, PageRequest.of(1, 1000), 2000);

        when(productRepo.findAllActiveProducts(PageRequest.of(0, 1000))).thenReturn(page1);
        when(productRepo.findAllActiveProducts(PageRequest.of(1, 1000))).thenReturn(page2);
        when(pricingEngine.calculateDynamicPrice(any(Product.class)))
                .thenReturn(new BigDecimal("110.00"))
                .thenReturn(new BigDecimal("120.00"));

        // Act
        schedulerService.scheduleDynamicPricing();

        // Assert
        verify(productRepo, times(2)).findAllActiveProducts(any(Pageable.class));
        verify(pricingEngine, times(2)).calculateDynamicPrice(any(Product.class));
        verify(productService, times(2)).updateProductPrice(anyInt(), any(BigDecimal.class));
        verify(entityManager, times(2)).flush();
        verify(entityManager, times(2)).clear();
    }

    @Test
    void testScheduleDynamicPricing_partialSuccess() {
        // Arrange
        List<Product> products = new ArrayList<>();
        Product product1 = new Product();
        product1.setProdId(1);
        Product product2 = new Product();
        product2.setProdId(2);
        products.add(product1);
        products.add(product2);

        Page<Product> productPage = new PageImpl<>(products, PageRequest.of(0, 1000), 2);
        when(productRepo.findAllActiveProducts(any(Pageable.class))).thenReturn(productPage);
        when(pricingEngine.calculateDynamicPrice(product1)).thenReturn(new BigDecimal("110.00"));
        when(pricingEngine.calculateDynamicPrice(product2)).thenThrow(new RuntimeException("Processing error"));

        // Act
        schedulerService.scheduleDynamicPricing();

        // Assert
        verify(productService, times(1)).updateProductPrice(1, new BigDecimal("110.00"));
        verify(productService, never()).updateProductPrice(eq(2), any(BigDecimal.class));
        verify(jobLogService, times(1)).updateLogs(any(JobDTO.class));
    }

    @Test
    void testScheduleDynamicPricing_batchFlushAndClear() {
        // Arrange
        List<Product> products = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Product product = new Product();
            product.setProdId(i);
            products.add(product);
        }

        Page<Product> productPage = new PageImpl<>(products, PageRequest.of(0, 1000), 5);
        when(productRepo.findAllActiveProducts(any(Pageable.class))).thenReturn(productPage);
        when(pricingEngine.calculateDynamicPrice(any(Product.class))).thenReturn(new BigDecimal("110.00"));

        // Act
        schedulerService.scheduleDynamicPricing();

        // Assert
        verify(entityManager, times(1)).flush();
        verify(entityManager, times(1)).clear();
        verify(productService, times(5)).updateProductPrice(anyInt(), any(BigDecimal.class));
    }

    @Test
    void testScheduleDynamicPricing_jobLogUpdated() {
        // Arrange
        List<Product> products = new ArrayList<>();
        Product product = new Product();
        product.setProdId(1);
        products.add(product);

        Page<Product> productPage = new PageImpl<>(products, PageRequest.of(0, 1000), 1);
        when(productRepo.findAllActiveProducts(any(Pageable.class))).thenReturn(productPage);
        when(pricingEngine.calculateDynamicPrice(product)).thenReturn(new BigDecimal("110.00"));

        // Act
        schedulerService.scheduleDynamicPricing();

        // Assert
        verify(jobLogService, times(1)).updateLogs(any(JobDTO.class));
    }

    @Test
    void testScheduleDynamicPricing_largeProductSet() {
        // Arrange
        List<Product> products = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            Product product = new Product();
            product.setProdId(i);
            products.add(product);
        }

        Page<Product> productPage = new PageImpl<>(products, PageRequest.of(0, 1000), 100);
        when(productRepo.findAllActiveProducts(any(Pageable.class))).thenReturn(productPage);
        when(pricingEngine.calculateDynamicPrice(any(Product.class))).thenReturn(new BigDecimal("110.00"));

        // Act
        schedulerService.scheduleDynamicPricing();

        // Assert
        verify(productService, times(100)).updateProductPrice(anyInt(), any(BigDecimal.class));
        verify(jobLogService, times(1)).updateLogs(any(JobDTO.class));
    }
}