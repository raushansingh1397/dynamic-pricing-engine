package com.example.dps.controller;

import com.example.dps.dto.RestockRequest;
import com.example.dps.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class InventoryControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockitoBean
    private InventoryService inventoryService;

    @Autowired
    private ObjectMapper objectMapper;
    @BeforeEach
    void setup() {
        // Explicitly apply Spring Security filter chain to MockMvc in Boot 4
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testProductRestock_success() throws Exception {
        // Arrange
        RestockRequest request = new RestockRequest();
        request.setQuantity(50);

        doNothing().when(inventoryService).productRestock(1, 50);

        // Act & Assert
        mockMvc.perform(patch("/dynamicPricing/inventory/1/restock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Product restocked successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testProductRestock_largeQuantity() throws Exception {
        // Arrange
        RestockRequest request = new RestockRequest();
        request.setQuantity(1000);

        doNothing().when(inventoryService).productRestock(1, 1000);

        // Act & Assert
        mockMvc.perform(patch("/dynamicPricing/inventory/1/restock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Product restocked successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testProductRestock_smallQuantity() throws Exception {
        // Arrange
        RestockRequest request = new RestockRequest();
        request.setQuantity(1);

        doNothing().when(inventoryService).productRestock(1, 1);

        // Act & Assert
        mockMvc.perform(patch("/dynamicPricing/inventory/1/restock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Product restocked successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testProductRestock_zeroQuantity() throws Exception {
        // Arrange
        RestockRequest request = new RestockRequest();
        request.setQuantity(0);

        doNothing().when(inventoryService).productRestock(1, 0);

        // Act & Assert
        mockMvc.perform(patch("/dynamicPricing/inventory/1/restock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testProductRestock_differentProductIds() throws Exception {
        // Arrange & Act & Assert
        for (int i = 1; i <= 5; i++) {
            RestockRequest request = new RestockRequest();
            request.setQuantity(50);

            doNothing().when(inventoryService).productRestock(i, 50);

            mockMvc.perform(patch("/dynamicPricing/inventory/" + i + "/restock")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value("Product restocked successfully"));
        }
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testProductRestock_productNotFound() throws Exception {
        // Arrange
        RestockRequest request = new RestockRequest();
        request.setQuantity(50);

        doThrow(new RuntimeException("Product not found"))
                .when(inventoryService).productRestock(999, 50);

        // Act & Assert
        mockMvc.perform(patch("/dynamicPricing/inventory/999/restock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testProductRestock_multipleRestocks() throws Exception {
        // Arrange
        RestockRequest request1 = new RestockRequest();
        request1.setQuantity(100);

        RestockRequest request2 = new RestockRequest();
        request2.setQuantity(200);

        doNothing().when(inventoryService).productRestock(1, 100);
        doNothing().when(inventoryService).productRestock(1, 200);

        // Act & Assert - First restock
        mockMvc.perform(patch("/dynamicPricing/inventory/1/restock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk());

        // Second restock
        mockMvc.perform(patch("/dynamicPricing/inventory/1/restock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testProductRestock_negativeQuantity() throws Exception {
        // Arrange
        RestockRequest request = new RestockRequest();
        request.setQuantity(-50);

        doNothing().when(inventoryService).productRestock(1, -50);

        // Act & Assert
        mockMvc.perform(patch("/dynamicPricing/inventory/1/restock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}

