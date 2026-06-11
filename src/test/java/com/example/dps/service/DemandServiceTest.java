package com.example.dps.service;

import com.example.dps.entity.Demand;
import com.example.dps.repository.DemandRepo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemandServiceTest {
    @Mock
    private DemandRepo demandRepo;

    @InjectMocks
    private DemandService demandService;

    @Test
    void getDemandScore_ShouldReturnDemandValue_WhenDemandIdExists() {
        // Given
        Integer demandId = 1;
        Integer expectedDemandValue = 5;
        when(demandRepo.findById(demandId)).thenReturn(Optional.of(new Demand(demandId, "High", expectedDemandValue)));

        // When
        Integer actualDemandValue = demandService.getDemandScore(demandId);

        // Then
        assertEquals(expectedDemandValue, actualDemandValue);
    }

    @Test
    void getDemandScore_ShouldThrowResourceNotFoundException_WhenDemandIdDoesNotExist() {
        // Given
        Integer demandId = 999;
        when(demandRepo.findById(demandId)).thenReturn(Optional.empty());

        // When & Then
        try {
            demandService.getDemandScore(demandId);
        } catch (Exception e) {
            assertEquals("Demand type not found!", e.getMessage());
        }
    }
}
