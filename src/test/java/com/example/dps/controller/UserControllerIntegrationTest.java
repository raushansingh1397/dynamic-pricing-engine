package com.example.dps.controller;

import com.example.dps.dto.UserDTO;
import com.example.dps.service.UserService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

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
    void testRegisterUser_success() throws Exception {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testuser");
        userDTO.setPassword("password123");
        userDTO.setRoles(null);

        when(userService.registerUser(any(UserDTO.class)))
                .thenReturn("User registered successfully!");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").value("User registered successfully!"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testRegisterUser_usernameAlreadyTaken() throws Exception {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("existinguser");
        userDTO.setPassword("password123");

        when(userService.registerUser(any(UserDTO.class)))
                .thenThrow(new IllegalArgumentException("Username is already taken!"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Username is already taken!"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testRegisterUser_roleNotFound() throws Exception {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("newuser");
        userDTO.setPassword("password123");
        userDTO.setRoles(List.of("ROLE_NONEXISTENT"));

        when(userService.registerUser(any(UserDTO.class)))
                .thenThrow(new RuntimeException("Role does not exist."));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$").value("Role does not exist."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testRegisterUser_withRoles() throws Exception {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("adminuser");
        userDTO.setPassword("password123");
        userDTO.setRoles(List.of("ROLE_ADMIN", "ROLE_USER"));

        when(userService.registerUser(any(UserDTO.class)))
                .thenReturn("User registered successfully!");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").value("User registered successfully!"));
    }

    @Test
    void testRegisterUser_emptyUsername() throws Exception {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("");
        userDTO.setPassword("password123");

        when(userService.registerUser(any(UserDTO.class)))
                .thenThrow(new RuntimeException("Username cannot be empty"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testRegisterUser_weakPassword() throws Exception {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testuser");
        userDTO.setPassword("pass");

        when(userService.registerUser(any(UserDTO.class)))
                .thenReturn("User registered successfully!");

        // Act & Assert - System should accept even weak password and handle validation elsewhere
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    void testRegisterUser_multipleUsers() throws Exception {
        // Arrange
        UserDTO userDTO1 = new UserDTO();
        userDTO1.setUsername("user1");
        userDTO1.setPassword("password123");

        UserDTO userDTO2 = new UserDTO();
        userDTO2.setUsername("user2");
        userDTO2.setPassword("password456");

        when(userService.registerUser(any(UserDTO.class)))
                .thenReturn("User registered successfully!");

        // Act & Assert - Register first user
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO1)))
                .andExpect(status().isCreated());

        // Register second user
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO2)))
                .andExpect(status().isCreated());
    }
}

