package com.example.dps.controller;

import com.example.dps.dto.ProductDTO;
import com.example.dps.entity.Product;
import com.example.dps.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

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
    void testFetchAllAvailableProducts_success() throws Exception {
        // Arrange
        List<ProductDTO> products = new ArrayList<>();
        ProductDTO product1 = new ProductDTO();
        product1.setProdId(1);
        product1.setProdName("Laptop");
        product1.setProdCategory("Electronics");
        products.add(product1);

        Page<ProductDTO> page = new PageImpl<>(products, PageRequest.of(0, 10), 1);
        when(productService.getAllProducts(0, 10)).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/dynamicPricing/product")
                        .param("pageNumber", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].prodId").value(1))
                .andExpect(jsonPath("$.content[0].prodName").value("Laptop"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testFetchProductByProdId_success() throws Exception {
        // Arrange
        ProductDTO product = new ProductDTO();
        product.setProdId(1);
        product.setProdName("Monitor");
        product.setProdCategory("Electronics");

        when(productService.getProductById(1)).thenReturn(product);

        // Act & Assert
        mockMvc.perform(get("/dynamicPricing/product/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prodId").value(1))
                .andExpect(jsonPath("$.prodName").value("Monitor"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddProduct_success() throws Exception {
        // Arrange
        Product product = new Product();
        product.setProdName("New Laptop");
        product.setProdCategory("Electronics");
        product.setBasePrice(new BigDecimal("999.99"));

        ProductDTO productDTO = new ProductDTO();
        productDTO.setProdId(1);
        productDTO.setProdName("New Laptop");

        when(productService.addProduct(any(Product.class))).thenReturn(productDTO);

        // Act & Assert
        mockMvc.perform(post("/dynamicPricing/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.prodId").value(1))
                .andExpect(jsonPath("$.prodName").value("New Laptop"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateProductDetails_success() throws Exception {
        // Arrange
        Product updatedProduct = new Product();
        updatedProduct.setProdName("Updated Laptop");
        updatedProduct.setBasePrice(new BigDecimal("1099.99"));

        ProductDTO productDTO = new ProductDTO();
        productDTO.setProdId(1);
        productDTO.setProdName("Updated Laptop");

        // FIXED: Using matchers eq(1) and any(Product.class)
        when(productService.updateProduct(eq(1), any(Product.class))).thenReturn(productDTO);

        // Act & Assert
        mockMvc.perform(put("/dynamicPricing/product/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prodId").value(1))
                .andExpect(jsonPath("$.prodName").value("Updated Laptop"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteProduct_success() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/dynamicPricing/product/1"))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).deleteProduct(1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testFetchAllAvailableProducts_emptyResult() throws Exception {
        // Arrange
        Page<ProductDTO> emptyPage = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 10), 0);
        when(productService.getAllProducts(0, 10)).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/dynamicPricing/product")
                        .param("pageNumber", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testFetchAllAvailableProducts_defaultPagination() throws Exception {
        // Arrange
        List<ProductDTO> products = new ArrayList<>();
        Page<ProductDTO> page = new PageImpl<>(products, PageRequest.of(0, 10), 0);
        when(productService.getAllProducts(0, 10)).thenReturn(page);

        // Act & Assert - test default params
        mockMvc.perform(get("/dynamicPricing/product"))
                .andExpect(status().isOk());
    }
}