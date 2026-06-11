package com.example.dps.service;

import com.example.dps.entity.Inventory;
import com.example.dps.entity.Product;
import com.example.dps.repository.InventoryRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {
    @Mock
    private InventoryRepo inventoryRepo;

    @Mock
    private Product product;

    @InjectMocks
    private InventoryService inventoryService;

    @Mock
    private Inventory inventory;
    @Test
    void productRestock_success() {
        // Given
        int prodId = 1;
        int quantity = 10;
        int initialCount = 5;
        int expectedCount = initialCount + quantity;

        Inventory inventory = new Inventory();
        inventory.setProdId(prodId);
        inventory.setProdCount(initialCount);

        when(inventoryRepo.getProductById(prodId)).thenReturn(Optional.of(inventory));
        // When
        inventoryService.productRestock(prodId, quantity);

        // assertions to verify the expected behavior
        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryRepo).save((inventoryCaptor.capture()));

        Inventory savedInventory = inventoryCaptor.getValue();
        assertEquals(expectedCount, savedInventory.getProdCount());
    }

    @Test
    void productRestock_throwsResourceNotFoundException() {
        // Given
        int prodId = 1;
        int quantity = 10;

        when(inventoryRepo.getProductById(prodId)).thenReturn(Optional.empty());

        // When & Then
        try {
            inventoryService.productRestock(prodId, quantity);
        } catch (Exception e) {
            assertEquals("No such product found!", e.getMessage());
        }
    }

    @Test
    void updateProductCount_success() {
        // Given
        int prodId = 1;
        int quantityPurchased = 3;
        int initialCount = 10;
        int expectedCount = initialCount - quantityPurchased;

        Inventory inventory = new Inventory();
        inventory.setProdId(prodId);
        inventory.setProdCount(initialCount);

        when(inventoryRepo.getProductById(prodId)).thenReturn(Optional.of(inventory));

        // When
        inventoryService.updateProductCount(prodId, quantityPurchased);

        // Then
        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryRepo).save(inventoryCaptor.capture());

        Inventory savedInventory = inventoryCaptor.getValue();
        assertEquals(expectedCount, savedInventory.getProdCount());
    }

    @Test
    void updateProductCount_throwsConflictException_OutOfStock() {
        // Given
        int prodId = 1;
        int quantityPurchased = 3;
        int initialCount = 0;

        Inventory inventory = new Inventory();
        inventory.setProdId(prodId);
        inventory.setProdCount(initialCount);

        when(inventoryRepo.getProductById(prodId)).thenReturn(Optional.of(inventory));

        // When & Then
        try {
            inventoryService.updateProductCount(prodId, quantityPurchased);
        } catch (Exception e) {
            assertEquals("Out Of Stock!!", e.getMessage());
        }
    }

    @Test
    void updateProductCount_throwsConflictException_InsufficientStock() {
        // Given
        int prodId = 1;
        int quantityPurchased = 5;
        int initialCount = 3;

        Inventory inventory = new Inventory();
        inventory.setProdId(prodId);
        inventory.setProdCount(initialCount);

        when(inventoryRepo.getProductById(prodId)).thenReturn(Optional.of(inventory));

        // When & Then
        try {
            inventoryService.updateProductCount(prodId, quantityPurchased);
        } catch (Exception e) {
            assertEquals("Insufficient stock!!", e.getMessage());
        }
    }

    @Test
    void getStockCount_success() {
        // Given
        int prodId = 1;
        int expectedCount = 10;

        Inventory inventory = new Inventory();
        inventory.setProdId(prodId);
        inventory.setProdCount(expectedCount);

        when(inventoryRepo.getProductById(prodId)).thenReturn(Optional.of(inventory));

        // When
        Integer actualCount = inventoryService.getStockCount(prodId);

        // Then
        assertEquals(expectedCount, actualCount);
    }

    @Test
    void getStockCount_throwsResourceNotFoundException() {
        // Given
        int prodId = 1;

        when(inventoryRepo.getProductById(prodId)).thenReturn(Optional.empty());

        // When & Then
        try {
            inventoryService.getStockCount(prodId);
        } catch (Exception e) {
            assertEquals("No Such product found!!", e.getMessage());
        }
    }

}
