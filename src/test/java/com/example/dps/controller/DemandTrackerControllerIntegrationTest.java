package com.example.dps.controller;

import com.example.dps.dto.PurchaseRequest;
import com.example.dps.service.TrackDemandService;
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

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DemandTrackerControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;


    @MockitoBean
    private TrackDemandService trackDemandService;

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
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testRecordView_success() throws Exception {
        // Arrange
        Integer prodId = 1;
        doNothing().when(trackDemandService).recordView(prodId);

        // Act & Assert
        mockMvc.perform(post("/dynamicPricing/demandTracker/1/view"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("View recorded"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testRecordAddToCart_success() throws Exception {
        // Arrange
        Integer prodId = 1;
        doNothing().when(trackDemandService).recordAddToCart(prodId);

        // Act & Assert
        mockMvc.perform(post("/dynamicPricing/demandTracker/1/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Add to cart recorded"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testRecordPurchase_success() throws Exception {
        // Arrange
        Integer prodId = 1;
        PurchaseRequest request = new PurchaseRequest(5);

        doNothing().when(trackDemandService).recordPurchase(prodId, 5);

        // Act & Assert
        mockMvc.perform(post("/dynamicPricing/demandTracker/1/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Purchase recorded"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testRecordView_multipleProducts() throws Exception {
        // Arrange & Act & Assert
        for (int i = 1; i <= 5; i++) {
            doNothing().when(trackDemandService).recordView(i);

            mockMvc.perform(post("/dynamicPricing/demandTracker/" + i + "/view"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value("View recorded"));
        }
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testRecordAddToCart_multipleProducts() throws Exception {
        // Arrange & Act & Assert
        for (int i = 1; i <= 5; i++) {
            doNothing().when(trackDemandService).recordAddToCart(i);

            mockMvc.perform(post("/dynamicPricing/demandTracker/" + i + "/cart"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value("Add to cart recorded"));
        }
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testRecordPurchase_largeQuantity() throws Exception {
        // Arrange
        Integer prodId = 1;
        PurchaseRequest request = new PurchaseRequest(1000);

        doNothing().when(trackDemandService).recordPurchase(prodId, 1000);

        // Act & Assert
        mockMvc.perform(post("/dynamicPricing/demandTracker/1/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Purchase recorded"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testRecordPurchase_smallQuantity() throws Exception {
        // Arrange
        Integer prodId = 1;
        PurchaseRequest request = new PurchaseRequest(1);

        doNothing().when(trackDemandService).recordPurchase(prodId, 1);

        // Act & Assert
        mockMvc.perform(post("/dynamicPricing/demandTracker/1/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Purchase recorded"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testRecordPurchase_zeroQuantity() throws Exception {
        // Arrange
        Integer prodId = 1;
        PurchaseRequest request = new PurchaseRequest(0);

        doNothing().when(trackDemandService).recordPurchase(prodId, 0);

        // Act & Assert
        mockMvc.perform(post("/dynamicPricing/demandTracker/1/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testRecordView_productNotFound() throws Exception {
        // Arrange
        Integer prodId = 999;
        doThrow(new RuntimeException("Product not found"))
                .when(trackDemandService).recordView(prodId);

        // Act & Assert
        mockMvc.perform(post("/dynamicPricing/demandTracker/999/view"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testRecordAddToCart_productNotFound() throws Exception {
        // Arrange
        Integer prodId = 999;
        doThrow(new RuntimeException("Product not found"))
                .when(trackDemandService).recordAddToCart(prodId);

        // Act & Assert
        mockMvc.perform(post("/dynamicPricing/demandTracker/999/cart"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testRecordPurchase_productNotFound() throws Exception {
        // Arrange
        Integer prodId = 999;
        PurchaseRequest request = new PurchaseRequest(5);

        doThrow(new RuntimeException("Product not found"))
                .when(trackDemandService).recordPurchase(prodId, 5);

        // Act & Assert
        mockMvc.perform(post("/dynamicPricing/demandTracker/999/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testRecordPurchase_multiplePurchases() throws Exception {
        // Arrange
        Integer prodId = 1;

        doNothing().when(trackDemandService).recordPurchase(anyInt(), anyInt());

        // Act & Assert
        for (int i = 1; i <= 3; i++) {
            PurchaseRequest request = new PurchaseRequest(i * 10);
            mockMvc.perform(post("/dynamicPricing/demandTracker/1/purchase")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value("Purchase recorded"));
        }
    }
}

