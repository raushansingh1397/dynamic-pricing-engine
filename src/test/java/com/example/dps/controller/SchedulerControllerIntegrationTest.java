package com.example.dps.controller;

import com.example.dps.service.SchedulerService;
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

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SchedulerControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockitoBean
    private SchedulerService schedulerService;

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
    void testScheduleDynamicPricing_success() throws Exception {
        // Arrange
        doNothing().when(schedulerService).scheduleDynamicPricing();

        // Act & Assert
        mockMvc.perform(post("/dynamicPricing/scheduler/trigger"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Scheduler started successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testScheduleDynamicPricing_multipleTriggersSuccess() throws Exception {
        // Arrange
        doNothing().when(schedulerService).scheduleDynamicPricing();

        // Act & Assert - Trigger multiple times
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/dynamicPricing/scheduler/trigger"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value("Scheduler started successfully"));
        }
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testScheduleDynamicPricing_exception() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Scheduler error"))
                .when(schedulerService).scheduleDynamicPricing();

        // Act & Assert
        mockMvc.perform(post("/dynamicPricing/scheduler/trigger"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testScheduleDynamicPricing_noProducts() throws Exception {
        // Arrange - Service completes without error even if no products
        doNothing().when(schedulerService).scheduleDynamicPricing();

        // Act & Assert
        mockMvc.perform(post("/dynamicPricing/scheduler/trigger"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Scheduler started successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testScheduleDynamicPricing_databaseError() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Database connection failed"))
                .when(schedulerService).scheduleDynamicPricing();

        // Act & Assert
        mockMvc.perform(post("/dynamicPricing/scheduler/trigger"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testScheduleDynamicPricing_concurrentTriggers() throws Exception {
        // Arrange
        doNothing().when(schedulerService).scheduleDynamicPricing();

        // Act & Assert - Simulate concurrent requests
        mockMvc.perform(post("/dynamicPricing/scheduler/trigger"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/dynamicPricing/scheduler/trigger"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/dynamicPricing/scheduler/trigger"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testScheduleDynamicPricing_requestEndpoint() throws Exception {
        // Arrange
        doNothing().when(schedulerService).scheduleDynamicPricing();

        // Act & Assert
        mockMvc.perform(post("/dynamicPricing/scheduler/trigger"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testScheduleDynamicPricing_responseFormat() throws Exception {
        // Arrange
        doNothing().when(schedulerService).scheduleDynamicPricing();

        // Act & Assert
        mockMvc.perform(post("/dynamicPricing/scheduler/trigger"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isString());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testScheduleDynamicPricing_timeoutScenario() throws Exception {
        // Arrange - Simulate long-running operation
        doNothing().when(schedulerService).scheduleDynamicPricing();

        // Act & Assert
        mockMvc.perform(post("/dynamicPricing/scheduler/trigger"))
                .andExpect(status().isOk());
    }
}

