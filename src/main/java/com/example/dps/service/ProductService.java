package com.example.dps.service;

import com.example.dps.record.PriceChangeEvent;
import com.example.dps.dto.ProductDTO;
import com.example.dps.entity.Inventory;
import com.example.dps.entity.Product;
import com.example.dps.exception.ConflictException;
import com.example.dps.exception.ResourceNotFoundException;
import com.example.dps.repository.ProductRepo;
import com.example.dps.utils.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepo repo;
    private final ApplicationEventPublisher eventPublisher;

    public ProductDTO addProduct(Product product){
        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setProdCount(1);
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

        if(product.getDiscountedPrice() !=  null && !product.getDiscountedPrice().equals(existing.getDiscountedPrice())){
            eventPublisher.publishEvent(new PriceChangeEvent(this, prodId, product.getDiscountedPrice(),existing.getDiscountedPrice(),product.getBasePrice(), Constants.MANUAL));
//            priceService.recordPriceChange(prodId,product.getCurrentDynamicPrice(),Constants.MANUAL);
        }
        existing.setDiscountedPrice(product.getDiscountedPrice());
        Product prod = repo.save(existing);

        return ProductDTO.createProdObj(prod, prod.getInventory());
    }

    public Page<ProductDTO> getAllProducts(int pageNumber,int size){
        Pageable pageable = PageRequest.of(pageNumber,size);
        Page<Product> products = repo.findAllActiveProducts(pageable);
        return products.map(prod->ProductDTO.createProdObj(prod,prod.getInventory()));
    }

    public ProductDTO getProductById(Integer prodId){
        Product prod = repo.findById(prodId).orElseThrow(()->new ResourceNotFoundException("Product not found!!"));
        return ProductDTO.createProdObj(prod,prod.getInventory());
    }

    public void updateProductPrice(int prodId,BigDecimal newPrice){
        Product prod = repo.findById(prodId).orElseThrow(()-> new ResourceNotFoundException("No such record found"));
        BigDecimal oldPrice = prod.getCurrentDynamicPrice();
        prod.setCurrentDynamicPrice(newPrice);
        repo.save(prod);
        eventPublisher.publishEvent(new PriceChangeEvent(this, prodId,newPrice, oldPrice,prod.getBasePrice(), Constants.SCHEDULER));
//        priceService.recordPriceChange(prodId,newPrice, Constants.SCHEDULER);
    }

    public void deleteProduct(Integer prodId){
        Product prod = repo.findById(prodId).orElseThrow(() -> new ResourceNotFoundException("No Such record found"));
        if(!prod.getIsActive()) throw new ConflictException("Product already inactive");
        prod.setIsActive(false);
        repo.save(prod);
    }


}
