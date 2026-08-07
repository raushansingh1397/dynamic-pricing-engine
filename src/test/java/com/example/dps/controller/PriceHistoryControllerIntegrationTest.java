package com.example.dps.controller;


import com.example.dps.entity.PriceHistory;
import com.example.dps.service.PriceHistoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PriceHistoryControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockitoBean
    private PriceHistoryService priceHistoryService;

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
    void testGetHistory_success() throws Exception {
        // Arrange
        Integer prodId = 1;
        List<PriceHistory> histories = new ArrayList<>();

        PriceHistory history1 = new PriceHistory();
        history1.setHistoryId(1);
        history1.setDiscountedPrice(new BigDecimal("100.00"));
        history1.setCalculatedAt(LocalDateTime.now());
        histories.add(history1);

        PriceHistory history2 = new PriceHistory();
        history2.setHistoryId(2);
        history2.setDiscountedPrice(new BigDecimal("105.00"));
        history2.setCalculatedAt(LocalDateTime.now());
        histories.add(history2);

        when(priceHistoryService.getHistory(prodId)).thenReturn(histories);

        // Act & Assert
        mockMvc.perform(get("/dynamicPricing/priceHistory/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].discountedPrice").value(100.00))
                .andExpect(jsonPath("$[1].discountedPrice").value(105.00));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetHistory_empty() throws Exception {
        // Arrange
        Integer prodId = 1;
        when(priceHistoryService.getHistory(prodId)).thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(get("/dynamicPricing/priceHistory/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetHistory_singleEntry() throws Exception {
        // Arrange
        Integer prodId = 2;
        List<PriceHistory> histories = new ArrayList<>();

        PriceHistory history = new PriceHistory();
        history.setHistoryId(1);
        history.setDiscountedPrice(new BigDecimal("99.99"));
        history.setCalculatedAt(LocalDateTime.now());
        histories.add(history);

        when(priceHistoryService.getHistory(prodId)).thenReturn(histories);

        // Act & Assert
        mockMvc.perform(get("/dynamicPricing/priceHistory/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].discountedPrice").value(99.99));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetHistory_multipleEntries() throws Exception {
        // Arrange
        Integer prodId = 3;
        List<PriceHistory> histories = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            PriceHistory history = new PriceHistory();
            history.setHistoryId(i + 1);
            history.setDiscountedPrice(new BigDecimal(100.00 + i));
            history.setCalculatedAt(LocalDateTime.now());
            histories.add(history);
        }

        when(priceHistoryService.getHistory(prodId)).thenReturn(histories);

        // Act & Assert
        mockMvc.perform(get("/dynamicPricing/priceHistory/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(10));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetHistory_differentProductIds() throws Exception {
        // Arrange
        for (int prodId = 1; prodId <= 5; prodId++) {
            List<PriceHistory> histories = new ArrayList<>();
            PriceHistory history = new PriceHistory();
            history.setHistoryId(1);
            history.setDiscountedPrice(new BigDecimal(100 + prodId));
            histories.add(history);
            when(priceHistoryService.getHistory(prodId)).thenReturn(histories);
        }

        // Act & Assert
        for (int prodId = 1; prodId <= 5; prodId++) {
            mockMvc.perform(get("/dynamicPricing/priceHistory/" + prodId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetHistory_largeProductId() throws Exception {
        // Arrange
        Integer prodId = 999999;
        List<PriceHistory> histories = new ArrayList<>();
        when(priceHistoryService.getHistory(prodId)).thenReturn(histories);

        // Act & Assert
        mockMvc.perform(get("/dynamicPricing/priceHistory/999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetHistory_priceChanges() throws Exception {
        // Arrange
        Integer prodId = 1;
        List<PriceHistory> histories = new ArrayList<>();

        // Create history showing price increase
        PriceHistory history1 = new PriceHistory();
        history1.setHistoryId(1);
        history1.setDiscountedPrice(new BigDecimal("100.00"));
        histories.add(history1);

        PriceHistory history2 = new PriceHistory();
        history2.setHistoryId(2);
        history2.setDiscountedPrice(new BigDecimal("120.00"));
        histories.add(history2);

        PriceHistory history3 = new PriceHistory();
        history3.setHistoryId(3);
        history3.setDiscountedPrice(new BigDecimal("110.00"));
        histories.add(history3);

        when(priceHistoryService.getHistory(prodId)).thenReturn(histories);

        // Act & Assert
        mockMvc.perform(get("/dynamicPricing/priceHistory/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].discountedPrice").value(100.00))
                .andExpect(jsonPath("$[1].discountedPrice").value(120.00))
                .andExpect(jsonPath("$[2].discountedPrice").value(110.00));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetHistory_withTimestamps() throws Exception {
        // Arrange
        Integer prodId = 1;
        List<PriceHistory> histories = new ArrayList<>();

        LocalDateTime time1 = LocalDateTime.now().minusHours(2);
        LocalDateTime time2 = LocalDateTime.now().minusHours(1);
        LocalDateTime time3 = LocalDateTime.now();

        PriceHistory history1 = new PriceHistory();
        history1.setHistoryId(1);
        history1.setDiscountedPrice(new BigDecimal("100.00"));
        history1.setCalculatedAt(time1);
        histories.add(history1);

        PriceHistory history2 = new PriceHistory();
        history2.setHistoryId(2);
        history2.setDiscountedPrice(new BigDecimal("105.00"));
        history2.setCalculatedAt(time2);
        histories.add(history2);

        PriceHistory history3 = new PriceHistory();
        history3.setHistoryId(3);
        history3.setDiscountedPrice(new BigDecimal("103.00"));
        history3.setCalculatedAt(time3);
        histories.add(history3);

        when(priceHistoryService.getHistory(prodId)).thenReturn(histories);

        // Act & Assert
        mockMvc.perform(get("/dynamicPricing/priceHistory/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }
}
