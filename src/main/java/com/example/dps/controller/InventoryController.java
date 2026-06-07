package com.example.dps.controller;

import com.example.dps.dto.RestockRequest;
import com.example.dps.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dynamicPricing/inventory")
@Slf4j
public class InventoryController {
    private final InventoryService inventoryService;

    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PatchMapping("/{prodId}/restock")
    public ResponseEntity<String> productRestock(@PathVariable("prodId") Integer prodId, @RequestBody RestockRequest restockRequest){
        inventoryService.productRestock(prodId,restockRequest.getQuantity());
        log.info("product restocked success!");
        return ResponseEntity.ok("Product restocked successfully");
    }
}
