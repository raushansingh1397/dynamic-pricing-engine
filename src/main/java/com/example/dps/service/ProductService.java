package com.example.dps.service;

import com.example.dps.dto.ProductDTO;
import com.example.dps.entity.Inventory;
import com.example.dps.entity.Product;
import com.example.dps.exception.ConflictException;
import com.example.dps.exception.ResourceNotFoundException;
import com.example.dps.repository.ProductRepo;
import com.example.dps.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {
    private final ProductRepo repo;
    private final PriceHistoryService priceService;

    @Autowired
    public ProductService(ProductRepo repo, PriceHistoryService priceService) {
        this.repo = repo;
        this.priceService = priceService;
    }

    public ProductDTO addProduct(Product product){
        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setProdCount(0);
        product.setInventory(inventory);
        product.setIsActive(true);
        Product prod = repo.save(product);
        return ProductDTO.createProdObj(prod,inventory);
    }

    public ProductDTO updateProduct(int prodId,Product product){
        Product existing = repo.findById(prodId).orElseThrow(()-> new ResourceNotFoundException("Product not found!!"));
        if(!existing.getIsActive()) throw new ConflictException("Product is inactive");
        existing.setProdName(product.getProdName());
        existing.setProdCategory(product.getProdCategory());
        existing.setProdCompanyName(product.getProdCompanyName());
        existing.setBasePrice(product.getBasePrice());
        existing.setDiscountedPrice(product.getDiscountedPrice());
        Product prod = repo.save(existing);
        if(product.getDiscountedPrice() !=  null && !product.getDiscountedPrice().equals(existing.getDiscountedPrice())){
            priceService.recordPriceChange(prodId,product.getCurrentDynamicPrice(),Constants.MANUAL);
        }
        return ProductDTO.createProdObj(prod, prod.getInventory());
    }

    public List<ProductDTO> getAllProducts(){
        List<Product> products = repo.findAllActiveProducts();
        List<ProductDTO> productDTOS = new ArrayList<>();
        for(Product prod: products){
            ProductDTO obj = ProductDTO.createProdObj(prod, prod.getInventory());
            productDTOS.add(obj);
        }
        return productDTOS;
    }

    public ProductDTO getProductById(Integer prodId){
        Product prod = repo.findById(prodId).orElseThrow(()->new ResourceNotFoundException("Product not found!!"));
        return ProductDTO.createProdObj(prod,prod.getInventory());
    }

    public void updateProductPrice(int prodId,BigDecimal newPrice){
        Product prod = repo.findById(prodId).orElseThrow(()-> new ResourceNotFoundException("No such record found"));
        prod.setCurrentDynamicPrice(newPrice);
        repo.save(prod);
        priceService.recordPriceChange(prodId,newPrice, Constants.SCHEDULER);
    }

    public void deleteProduct(Integer prodId){
        Product prod = repo.findById(prodId).orElseThrow(() -> new ResourceNotFoundException("No Such record found"));
        if(!prod.getIsActive()) throw new ConflictException("Product already inactive");
        prod.setIsActive(false);
        repo.save(prod);
    }


}
