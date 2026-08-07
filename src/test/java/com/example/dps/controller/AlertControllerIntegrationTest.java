package com.example.dps.controller;

import com.example.dps.service.SendMailService;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AlertControllerIntegrationTest {
    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private SendMailService sendMailService;
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        // Explicitly apply Spring Security filter chain to MockMvc in Boot 4
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }


    @Test
    void testSendMail_success() throws Exception {
        // Arrange
        Integer prodId = 1;
        doNothing().when(sendMailService).sendEmailManually(prodId);

        // Act & Assert
        mockMvc.perform(post("/dynamicPricing/alerts/1/notify").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Mail sent successfully!!"));
    }

    @Test
    @WithMockUser(username="admin", roles = {"ADMIN"})
    void testSendMail_differentProductIds() throws Exception {
        // Arrange
        for (int i = 1; i <= 5; i++) {
            doNothing().when(sendMailService).sendEmailManually(i);
        }

        // Act & Assert
        for (int i = 1; i <= 5; i++) {
            mockMvc.perform(post("/dynamicPricing/alerts/" + i + "/notify"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value("Mail sent successfully!!"));
        }
    }

    @Test
    @WithMockUser(username="admin", roles = {"ADMIN"})
    void testSendMail_exception() throws Exception {
        // Arrange
        Integer prodId = 1;
        doThrow(new RuntimeException("Mail server error"))
                .when(sendMailService).sendEmailManually(prodId);

        // Act & Assert - Should still return OK as exception is caught
        mockMvc.perform(post("/dynamicPricing/alerts/1/notify"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$").value("Something went wrong: Mail server error"));
    }

    @Test
    @WithMockUser(username="admin", roles = {"ADMIN"})
    void testSendMail_largeProductId() throws Exception {
        // Arrange
        Integer prodId = 999999;
        doNothing().when(sendMailService).sendEmailManually(prodId);

        // Act & Assert
        mockMvc.perform(post("/dynamicPricing/alerts/999999/notify"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Mail sent successfully!!"));
    }

    @Test
    @WithMockUser(username="admin", roles = {"ADMIN"})
    void testSendMail_zeroProductId() throws Exception {
        // Arrange
        Integer prodId = 0;
        doNothing().when(sendMailService).sendEmailManually(prodId);

        // Act & Assert
        mockMvc.perform(post("/dynamicPricing/alerts/0/notify"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username="admin", roles = {"ADMIN"})
    void testSendMail_negativeProductId() throws Exception {
        // Arrange
        Integer prodId = -1;
        doNothing().when(sendMailService).sendEmailManually(prodId);

        // Act & Assert
        mockMvc.perform(post("/dynamicPricing/alerts/-1/notify"))
                .andExpect(status().isOk());
    }
}

