package com.example.dps.controller;

import com.example.dps.dto.PurchaseRequest;

import com.example.dps.service.TrackDemandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dynamicPricing/demandTracker")
@Slf4j
public class DemandTrackerController {

    private final TrackDemandService demandService;

    @Autowired
    public DemandTrackerController(TrackDemandService demandService) {
        this.demandService = demandService;
    }

    @PostMapping("/{prodId}/view")
    public ResponseEntity<?> recordView(@PathVariable("prodId") Integer prodId){
        demandService.recordView(prodId);
        log.info("View recorded for prodId= {}",prodId);
        return ResponseEntity.ok("View recorded");
    }

    @PostMapping("/{prodId}/cart")
    public ResponseEntity<?> recordAddToCart(@PathVariable("prodId") Integer prodId){
        demandService.recordAddToCart(prodId);
        log.info("Add to cart recorded for prodId= {}",prodId);
        return ResponseEntity.ok("Add to cart recorded");
    }

    @PostMapping("/{prodId}/purchase")
    public ResponseEntity<?> recordPurchase(@PathVariable("prodId") Integer prodId, @RequestBody PurchaseRequest request) {
        demandService.recordPurchase(prodId,request.quantity());
        log.info("Purchase recorded for prodId= {}",prodId);
        return ResponseEntity.ok("Purchase recorded");
    }
}
