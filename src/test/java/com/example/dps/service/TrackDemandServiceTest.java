package com.example.dps.service;

import com.example.dps.entity.Demand;
import com.example.dps.entity.Product;
import com.example.dps.entity.TrackProdDemand;
import com.example.dps.exception.ResourceNotFoundException;
import com.example.dps.repository.DemandRepo;
import com.example.dps.repository.ProductRepo;
import com.example.dps.repository.TrackProductDemandRepo;
import com.example.dps.utils.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackDemandServiceTest {

    @Mock
    private TrackProductDemandRepo repo;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private ProductRepo productRepo;

    @Mock
    private DemandRepo demandRepo;

    @InjectMocks
    private TrackDemandService trackDemandService;

    @Test
    void testGetProdDemands_success() {
        // Arrange
        Integer prodId = 1;
        List<TrackProdDemand> demands = new ArrayList<>();
        
        TrackProdDemand demand1 = new TrackProdDemand();
        demand1.setTrackId(1);
        demand1.setDemandCount(10);
        demands.add(demand1);

        when(repo.findTodayDemands(prodId)).thenReturn(demands);

        // Act
        List<TrackProdDemand> result = trackDemandService.getProdDemands(prodId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(10, result.get(0).getDemandCount());
        verify(repo, times(1)).findTodayDemands(prodId);
    }

    @Test
    void testGetProdDemands_empty() {
        // Arrange
        Integer prodId = 1;
        when(repo.findTodayDemands(prodId)).thenReturn(new ArrayList<>());

        // Act
        List<TrackProdDemand> result = trackDemandService.getProdDemands(prodId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repo, times(1)).findTodayDemands(prodId);
    }

    @Test
    void testRecordView_newRecord() {
        // Arrange
        Integer prodId = 1;
        when(repo.findTodayRecord(prodId, Constants.VIEW)).thenReturn(Optional.empty());

        Product product = new Product();
        product.setProdId(prodId);
        when(productRepo.findById(prodId)).thenReturn(Optional.of(product));

        Demand demand = new Demand();
        demand.setDemandId(Constants.VIEW);
        when(demandRepo.findById(Constants.VIEW)).thenReturn(Optional.of(demand));

        // Act
        trackDemandService.recordView(prodId);

        // Assert
        ArgumentCaptor<TrackProdDemand> captor = ArgumentCaptor.forClass(TrackProdDemand.class);
        verify(repo, times(1)).save(captor.capture());
        assertEquals(1, captor.getValue().getDemandCount());
        assertEquals(product, captor.getValue().getProduct());
    }

    @Test
    void testRecordView_existingRecord() {
        // Arrange
        Integer prodId = 1;
        TrackProdDemand existingRecord = new TrackProdDemand();
        existingRecord.setDemandCount(5);

        when(repo.findTodayRecord(prodId, Constants.VIEW)).thenReturn(Optional.of(existingRecord));

        // Act
        trackDemandService.recordView(prodId);

        // Assert
        ArgumentCaptor<TrackProdDemand> captor = ArgumentCaptor.forClass(TrackProdDemand.class);
        verify(repo, times(1)).save(captor.capture());
        assertEquals(6, captor.getValue().getDemandCount());
        verify(productRepo, never()).findById(anyInt());
    }

    @Test
    void testRecordAddToCart_newRecord() {
        // Arrange
        Integer prodId = 2;
        when(repo.findTodayRecord(prodId, Constants.ADD_TO_CART)).thenReturn(Optional.empty());

        Product product = new Product();
        product.setProdId(prodId);
        when(productRepo.findById(prodId)).thenReturn(Optional.of(product));

        Demand demand = new Demand();
        demand.setDemandId(Constants.ADD_TO_CART);
        when(demandRepo.findById(Constants.ADD_TO_CART)).thenReturn(Optional.of(demand));

        // Act
        trackDemandService.recordAddToCart(prodId);

        // Assert
        ArgumentCaptor<TrackProdDemand> captor = ArgumentCaptor.forClass(TrackProdDemand.class);
        verify(repo, times(1)).save(captor.capture());
        assertEquals(1, captor.getValue().getDemandCount());
    }

    @Test
    void testRecordAddToCart_existingRecord() {
        // Arrange
        Integer prodId = 2;
        TrackProdDemand existingRecord = new TrackProdDemand();
        existingRecord.setDemandCount(3);

        when(repo.findTodayRecord(prodId, Constants.ADD_TO_CART)).thenReturn(Optional.of(existingRecord));

        // Act
        trackDemandService.recordAddToCart(prodId);

        // Assert
        ArgumentCaptor<TrackProdDemand> captor = ArgumentCaptor.forClass(TrackProdDemand.class);
        verify(repo, times(1)).save(captor.capture());
        assertEquals(4, captor.getValue().getDemandCount());
    }

    @Test
    void testRecordPurchase_newRecord() {
        // Arrange
        Integer prodId = 3;
        Integer quantity = 5;
        when(repo.findTodayRecord(prodId, Constants.PURCHASE)).thenReturn(Optional.empty());

        Product product = new Product();
        product.setProdId(prodId);
        when(productRepo.findById(prodId)).thenReturn(Optional.of(product));

        Demand demand = new Demand();
        demand.setDemandId(Constants.PURCHASE);
        when(demandRepo.findById(Constants.PURCHASE)).thenReturn(Optional.of(demand));

        // Act
        trackDemandService.recordPurchase(prodId, quantity);

        // Assert
        verify(inventoryService, times(1)).updateProductCount(prodId, quantity);
        ArgumentCaptor<TrackProdDemand> captor = ArgumentCaptor.forClass(TrackProdDemand.class);
        verify(repo, times(1)).save(captor.capture());
        assertEquals(5, captor.getValue().getDemandCount());
    }

    @Test
    void testRecordPurchase_existingRecord() {
        // Arrange
        Integer prodId = 3;
        Integer quantity = 10;
        TrackProdDemand existingRecord = new TrackProdDemand();
        existingRecord.setDemandCount(5);

        when(repo.findTodayRecord(prodId, Constants.PURCHASE)).thenReturn(Optional.of(existingRecord));

        // Act
        trackDemandService.recordPurchase(prodId, quantity);

        // Assert
        verify(inventoryService, times(1)).updateProductCount(prodId, quantity);
        ArgumentCaptor<TrackProdDemand> captor = ArgumentCaptor.forClass(TrackProdDemand.class);
        verify(repo, times(1)).save(captor.capture());
        assertEquals(15, captor.getValue().getDemandCount());
    }

    @Test
    void testRecordDemandEvent_productNotFound() {
        // Arrange
        Integer prodId = 999;
        when(repo.findTodayRecord(prodId, Constants.VIEW)).thenReturn(Optional.empty());
        when(productRepo.findById(prodId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            trackDemandService.recordView(prodId);
        });
        verify(repo, never()).save(any(TrackProdDemand.class));
    }

    @Test
    void testRecordDemandEvent_demandNotFound() {
        // Arrange
        Integer prodId = 1;
        Product product = new Product();
        product.setProdId(prodId);

        when(repo.findTodayRecord(prodId, Constants.VIEW)).thenReturn(Optional.empty());
        when(productRepo.findById(prodId)).thenReturn(Optional.of(product));
        when(demandRepo.findById(Constants.VIEW)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            trackDemandService.recordView(prodId);
        });
        verify(repo, never()).save(any(TrackProdDemand.class));
    }

    @Test
    void testRecordPurchase_largeQuantity() {
        // Arrange
        Integer prodId = 5;
        Integer quantity = 1000;
        TrackProdDemand existingRecord = new TrackProdDemand();
        existingRecord.setDemandCount(500);

        when(repo.findTodayRecord(prodId, Constants.PURCHASE)).thenReturn(Optional.of(existingRecord));

        // Act
        trackDemandService.recordPurchase(prodId, quantity);

        // Assert
        verify(inventoryService, times(1)).updateProductCount(prodId, quantity);
        ArgumentCaptor<TrackProdDemand> captor = ArgumentCaptor.forClass(TrackProdDemand.class);
        verify(repo, times(1)).save(captor.capture());
        assertEquals(1500, captor.getValue().getDemandCount());
    }

    @Test
    void testRecordView_multipleRecords() {
        // Arrange
        Integer prodId = 1;

        when(repo.findTodayRecord(prodId, Constants.VIEW)).thenReturn(Optional.empty());
        Product product = new Product();
        product.setProdId(prodId);
        when(productRepo.findById(prodId)).thenReturn(Optional.of(product));

        Demand demand = new Demand();
        demand.setDemandId(Constants.VIEW);
        when(demandRepo.findById(Constants.VIEW)).thenReturn(Optional.of(demand));

        // Act
        trackDemandService.recordView(prodId);
        trackDemandService.recordView(prodId);

        // Assert
        verify(repo, times(2)).save(any(TrackProdDemand.class));
        verify(repo, times(2)).findTodayRecord(prodId, Constants.VIEW);
    }
}

