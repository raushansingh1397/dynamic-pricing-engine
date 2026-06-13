package com.example.dps.service;

import com.example.dps.dto.ProductDTO;
import com.example.dps.entity.Inventory;
import com.example.dps.entity.Product;
import com.example.dps.exception.ConflictException;
import com.example.dps.exception.ResourceNotFoundException;
import com.example.dps.repository.ProductRepo;
import com.example.dps.utils.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock
    private ProductRepo productRepo;

    @Mock
    private PriceHistoryService priceService;

    @InjectMocks
    private ProductService productService;

    // ===== TEST 1: addProduct =====
    @Test
    void testAddProduct_success() {
        // Arrange
        Product product = new Product();
        product.setProdId(null);  // New product
        product.setProdName("Laptop Pro");
        product.setProdCategory("Electronics");
        product.setProdCompanyName("TechCorp");
        product.setBasePrice(new BigDecimal("999.99"));
        product.setDiscountedPrice(new BigDecimal("899.99"));

        Product savedProduct = getSavedProduct();

        when(productRepo.save(any(Product.class))).thenReturn(savedProduct);

        // Act
        ProductDTO result = productService.addProduct(product);

        // Assert: Verify save was called
        verify(productRepo, times(1)).save(any(Product.class));

        // Assert: Verify the returned DTO
        assertNotNull(result);
        assertEquals(1, result.getProdId());
        assertEquals("Laptop Pro", result.getProdName());
        assertEquals("Electronics", result.getProdCategory());
        assertEquals("TechCorp", result.getProdCompanyName());

        // Assert: Verify prices
        assertEquals(new BigDecimal("899.99"), result.getDiscountedPrice());

        // Assert: Verify inventory initialization
        assertEquals(0, result.getProdCount());

        // Assert: Verify product was set to active
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepo).save(productCaptor.capture());
        assertTrue(productCaptor.getValue().getIsActive());
    }

    private static Product getSavedProduct() {
        Product savedProduct = new Product();
        savedProduct.setProdId(1);
        savedProduct.setProdName("Laptop Pro");
        savedProduct.setProdCategory("Electronics");
        savedProduct.setProdCompanyName("TechCorp");
        savedProduct.setBasePrice(new BigDecimal("999.99"));
        savedProduct.setDiscountedPrice(new BigDecimal("899.99"));
        savedProduct.setIsActive(true);

        Inventory inventory = new Inventory();
        inventory.setProdId(1);
        inventory.setProdCount(0);
        savedProduct.setInventory(inventory);
        return savedProduct;
    }

    // ===== TEST 2: getProductById =====
    @Test
    void testGetProductById_success() {
        // Arrange
        int prodId = 1;
        Product product = new Product();
        product.setProdId(prodId);
        product.setProdName("Monitor 4K");
        product.setBasePrice(new BigDecimal("399.99"));

        Inventory inventory = new Inventory();
        inventory.setProdId(prodId);
        inventory.setProdCount(100);
        product.setInventory(inventory);

        when(productRepo.findById(prodId)).thenReturn(Optional.of(product));

        // Act
        ProductDTO result = productService.getProductById(prodId);

        // Assert
        assertNotNull(result);
        assertEquals(prodId, result.getProdId());
        assertEquals("Monitor 4K", result.getProdName());
        assertEquals(100, result.getProdCount());
        verify(productRepo, times(1)).findById(prodId);
    }

    @Test
    void testGetProductById_notFound() {
        // Arrange
        int prodId = 999;
        when(productRepo.findById(prodId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            productService.getProductById(prodId);
        });
        verify(productRepo, times(1)).findById(prodId);
    }

    // ===== TEST 3: getAllProducts =====
    @Test
    void testGetAllProducts_success() {
        // Arrange
        List<Product> products = new ArrayList<>();

        Product product1 = new Product();
        product1.setProdId(1);
        product1.setProdName("Laptop");
        Inventory inv1 = new Inventory();
        inv1.setProdCount(50);
        product1.setInventory(inv1);

        Product product2 = new Product();
        product2.setProdId(2);
        product2.setProdName("Monitor");
        Inventory inv2 = new Inventory();
        inv2.setProdCount(100);
        product2.setInventory(inv2);

        products.add(product1);
        products.add(product2);

        when(productRepo.findAllActiveProducts()).thenReturn(products);

        // Act
        List<ProductDTO> result = productService.getAllProducts();

        // Assert: Verify list size
        assertNotNull(result);
        assertEquals(2, result.size());

        // Assert: Verify first product
        assertEquals(1, result.get(0).getProdId());
        assertEquals("Laptop", result.get(0).getProdName());
        assertEquals(50, result.get(0).getProdCount());

        // Assert: Verify second product
        assertEquals(2, result.get(1).getProdId());
        assertEquals("Monitor", result.get(1).getProdName());
        assertEquals(100, result.get(1).getProdCount());

        verify(productRepo, times(1)).findAllActiveProducts();
    }

    @Test
    void testGetAllProducts_empty() {
        // Arrange
        when(productRepo.findAllActiveProducts()).thenReturn(new ArrayList<>());

        // Act
        List<ProductDTO> result = productService.getAllProducts();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
    }

    // ===== TEST 4: updateProduct =====
    @Test
    void testUpdateProduct_success() {
        // Arrange
        int prodId = 1;
        Product existingProduct = new Product();
        existingProduct.setProdId(prodId);
        existingProduct.setProdName("Old Name");
        existingProduct.setProdCategory("Electronics");
        existingProduct.setBasePrice(new BigDecimal("100.00"));
        existingProduct.setDiscountedPrice(new BigDecimal("80.00"));
        existingProduct.setIsActive(true);

        Inventory inventory = new Inventory();
        inventory.setProdId(prodId);
        inventory.setProdCount(50);
        existingProduct.setInventory(inventory);

        Product updatedProduct = new Product();
        updatedProduct.setProdName("New Name");
        updatedProduct.setProdCategory("Accessories");
        updatedProduct.setBasePrice(new BigDecimal("150.00"));
        updatedProduct.setDiscountedPrice(new BigDecimal("120.00"));

        when(productRepo.findById(prodId)).thenReturn(Optional.of(existingProduct));
        when(productRepo.save(any(Product.class))).thenReturn(existingProduct);

        // Act
        ProductDTO result = productService.updateProduct(prodId, updatedProduct);

        // Assert: Verify repository calls
        verify(productRepo, times(1)).findById(prodId);
        verify(productRepo, times(1)).save(any(Product.class));

        // Assert: Verify updated fields
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepo).save(productCaptor.capture());
        assertEquals("New Name", productCaptor.getValue().getProdName());
        assertEquals("Accessories", productCaptor.getValue().getProdCategory());
        assertEquals(new BigDecimal("150.00"), productCaptor.getValue().getBasePrice());

        // Assert: Verify returned DTO
        assertNotNull(result);
        assertEquals(prodId, result.getProdId());
    }

    @Test
    void testUpdateProduct_productNotFound() {
        // Arrange
        int prodId = 999;
        Product updatedProduct = new Product();
        when(productRepo.findById(prodId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProduct(prodId, updatedProduct);
        });
    }

    @Test
    void testUpdateProduct_productInactive() {
        // Arrange
        int prodId = 1;
        Product existingProduct = new Product();
        existingProduct.setProdId(prodId);
        existingProduct.setIsActive(false);  // Inactive

        Product updatedProduct = new Product();

        when(productRepo.findById(prodId)).thenReturn(Optional.of(existingProduct));

        // Act & Assert
        assertThrows(ConflictException.class, () -> {
            productService.updateProduct(prodId, updatedProduct);
        });

        // Verify save was NOT called
        verify(productRepo, never()).save(any(Product.class));
    }

    @Test
    void testUpdateProduct_withPriceChange() {
        // Arrange
        int prodId = 1;
        Product existingProduct = new Product();
        existingProduct.setProdId(prodId);
        existingProduct.setProdName("Product");
        existingProduct.setDiscountedPrice(new BigDecimal("100.00"));
        existingProduct.setBasePrice(new BigDecimal("100.00"));
        existingProduct.setIsActive(true);

        Inventory inventory = new Inventory();
        inventory.setProdCount(0);
        existingProduct.setInventory(inventory);

        Product updatedProduct = new Product();
        updatedProduct.setDiscountedPrice(new BigDecimal("80.00"));  // Price changed
        updatedProduct.setCurrentDynamicPrice(new BigDecimal("85.00"));

        when(productRepo.findById(prodId)).thenReturn(Optional.of(existingProduct));
        when(productRepo.save(any(Product.class))).thenReturn(existingProduct);

        // Act
        productService.updateProduct(prodId, updatedProduct);

        // Assert: Verify price change was recorded
        verify(priceService, times(1)).recordPriceChange(
            eq(prodId),
            eq(new BigDecimal("85.00")),
            eq(Constants.MANUAL)
        );
    }

    // ===== TEST 5: updateProductPrice =====
    @Test
    void testUpdateProductPrice_success() {
        // Arrange
        int prodId = 1;
        BigDecimal newPrice = new BigDecimal("120.00");

        Product product = new Product();
        product.setProdId(prodId);
        product.setCurrentDynamicPrice(new BigDecimal("100.00"));

        when(productRepo.findById(prodId)).thenReturn(Optional.of(product));
        when(productRepo.save(any(Product.class))).thenReturn(product);

        // Act
        productService.updateProductPrice(prodId, newPrice);

        // Assert: Verify price was updated
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepo).save(productCaptor.capture());
        assertEquals(newPrice, productCaptor.getValue().getCurrentDynamicPrice());

        // Assert: Verify price change was recorded with SCHEDULER constant
        verify(priceService, times(1)).recordPriceChange(
            eq(prodId),
            eq(newPrice),
            eq(Constants.SCHEDULER)
        );
    }

    @Test
    void testUpdateProductPrice_notFound() {
        // Arrange
        int prodId = 999;
        BigDecimal newPrice = new BigDecimal("120.00");
        when(productRepo.findById(prodId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProductPrice(prodId, newPrice);
        });
    }

    // ===== TEST 6: deleteProduct =====
    @Test
    void testDeleteProduct_success() {
        // Arrange
        int prodId = 1;
        Product product = new Product();
        product.setProdId(prodId);
        product.setIsActive(true);

        when(productRepo.findById(prodId)).thenReturn(Optional.of(product));
        when(productRepo.save(any(Product.class))).thenReturn(product);

        // Act
        productService.deleteProduct(prodId);

        // Assert: Verify product was marked inactive
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepo).save(productCaptor.capture());
        assertFalse(productCaptor.getValue().getIsActive());

        // Assert: Verify repository calls
        verify(productRepo, times(1)).findById(prodId);
        verify(productRepo, times(1)).save(any(Product.class));
    }

    @Test
    void testDeleteProduct_notFound() {
        // Arrange
        int prodId = 999;
        when(productRepo.findById(prodId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            productService.deleteProduct(prodId);
        });
    }

    @Test
    void testDeleteProduct_alreadyInactive() {
        // Arrange
        int prodId = 1;
        Product product = new Product();
        product.setProdId(prodId);
        product.setIsActive(false);  // Already inactive

        when(productRepo.findById(prodId)).thenReturn(Optional.of(product));

        // Act & Assert
        assertThrows(ConflictException.class, () -> {
            productService.deleteProduct(prodId);
        });

        // Verify save was NOT called
        verify(productRepo, never()).save(any(Product.class));
    }
}