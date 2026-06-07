package com.example.dps.service;

import com.example.dps.entity.Inventory;
import com.example.dps.exception.ConflictException;
import com.example.dps.exception.ResourceNotFoundException;
import com.example.dps.repository.InventoryRepo;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {
    private final InventoryRepo inventoryRepo;

    public InventoryService(InventoryRepo inventoryRepo) {
        this.inventoryRepo = inventoryRepo;
    }

    public void productRestock(int prodId, int quantity){
        Inventory inventory = inventoryRepo.getProductById(prodId).orElseThrow(()-> new ResourceNotFoundException("No such product found!"));
        inventory.setProdCount(inventory.getProdCount()+ quantity);
        inventoryRepo.save(inventory);
    }

    public void updateProductCount(int prodId, int quantityPurchased){
        Inventory inventory = inventoryRepo.getProductById(prodId).orElseThrow(()->new ResourceNotFoundException("No such product found!!"));
        if(inventory.getProdCount() <= 0 ) throw  new ConflictException("Out Of Stock!!");
        if(inventory.getProdCount() - quantityPurchased < 0) throw new ConflictException("Insufficient stock!!");
        inventory.setProdCount(inventory.getProdCount()-quantityPurchased);
        inventoryRepo.save(inventory);
    }

    public Integer getStockCount(Integer prodId){
        return inventoryRepo.getProductById(prodId)
                .orElseThrow(()->new ResourceNotFoundException("No Such product found!!"))
                .getProdCount();
    }
}
