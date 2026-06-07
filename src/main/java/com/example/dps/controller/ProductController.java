package com.example.dps.controller;

import com.example.dps.dto.ProductDTO;
import com.example.dps.entity.Product;
import com.example.dps.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping
    public ResponseEntity<?> fetchAllAvailableProducts(){
        List<ProductDTO> dto = productService.getAllProducts();
        log.info("product list {}",dto);
        return ResponseEntity.ok(dto);

    }

    @GetMapping("/{prodId}")
    public ResponseEntity<?> fetchProductByProdId(@PathVariable("prodId") int prodId){
        ProductDTO product = productService.getProductById(prodId);
        log.info("Product : {}",product);
        return ResponseEntity.ok(product);
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<?> addProduct(@RequestBody Product product){
        ProductDTO productDTO = productService.addProduct(product);
        log.info("product added: {}", productDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(productDTO);

    }

    @PutMapping("/{prodId}")
    public ResponseEntity<?> updateProductDetails(@PathVariable("prodId") Integer prodId, @RequestBody Product product){
        ProductDTO productDTO = productService.updateProduct(prodId,product);
        log.info("product updated {}",productDTO);
        return ResponseEntity.ok(productDTO);

    }

    @DeleteMapping("/{prodId}")
    public ResponseEntity<String> deleteProduct(@PathVariable("prodId") int prodId){
        productService.deleteProduct(prodId);
        log.info("product deleted id= {}",prodId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
