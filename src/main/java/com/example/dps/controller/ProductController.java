package com.example.dps.controller;

import com.example.dps.dto.ProductDTO;
import com.example.dps.entity.Product;
import com.example.dps.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dynamicPricing/product")
@Slf4j
public class ProductController {
    private final ProductService productService;
    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping()
    @Operation(summary = "Get all the available products")
    public ResponseEntity<?> fetchAllAvailableProducts(@RequestParam(value = "pageNumber",defaultValue = "0") int pageNumber, @RequestParam(value = "size",defaultValue = "10") int size){
        Page<ProductDTO> dto = productService.getAllProducts(pageNumber,size);
        log.info("product list {}",dto);
        return ResponseEntity.ok(dto);

    }

    @GetMapping("/{prodId}")
    @Operation(summary = "Get all the available product by ID")
    public ResponseEntity<?> fetchProductByProdId(@PathVariable("prodId") int prodId){
        ProductDTO product = productService.getProductById(prodId);
        log.info("Product : {}",product);
        return ResponseEntity.ok(product);
    }

    @PostMapping(consumes = "application/json")
    @Operation(summary = "Add product")
    public ResponseEntity<?> addProduct(@RequestBody Product product){
        ProductDTO productDTO = productService.addProduct(product);
        log.info("product added: {}", productDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(productDTO);

    }

    @PutMapping("/{prodId}")
    @Operation(summary = "Update product details")
    public ResponseEntity<?> updateProductDetails(@PathVariable("prodId") Integer prodId, @RequestBody Product product){
        ProductDTO productDTO = productService.updateProduct(prodId,product);
        log.info("product updated {}",productDTO);
        return ResponseEntity.ok(productDTO);

    }

    @DeleteMapping("/{prodId}")
    @Operation(summary = "Delete the product" +
            "")
    public ResponseEntity<String> deleteProduct(@PathVariable("prodId") int prodId){
        productService.deleteProduct(prodId);
        log.info("product deleted id= {}",prodId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
