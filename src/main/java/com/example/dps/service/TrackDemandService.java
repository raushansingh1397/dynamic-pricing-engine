package com.example.dps.service;
import com.example.dps.entity.Demand;
import com.example.dps.entity.Product;
import com.example.dps.entity.TrackProdDemand;
import com.example.dps.exception.ResourceNotFoundException;
import com.example.dps.repository.DemandRepo;
import com.example.dps.repository.ProductRepo;
import com.example.dps.repository.TrackProductDemandRepo;
import com.example.dps.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrackDemandService {
    private final TrackProductDemandRepo repo;
    private final InventoryService inventoryService;
    private final ProductRepo productRepo;
    private final DemandRepo demandRepo;

    @Autowired
    public TrackDemandService(TrackProductDemandRepo repo, InventoryService inventoryService, ProductRepo productRepo, DemandRepo demandRepo) {
        this.repo = repo;
        this.inventoryService = inventoryService;
        this.productRepo = productRepo;
        this.demandRepo = demandRepo;
    }


    public List<TrackProdDemand> getProdDemands(Integer prodId){
        return  repo.findTodayDemands(prodId);
    }

    private void recordDemandEvent(Integer prodId,Integer demandId, Integer quantity){
        Optional<TrackProdDemand> prodDemand = repo.findTodayRecord(prodId,demandId);

        if(prodDemand.isPresent()){
            TrackProdDemand record = prodDemand.get();
            record.setDemandCount(record.getDemandCount()+quantity);
            repo.save(record);
        } else {
            TrackProdDemand record = new TrackProdDemand();
            Product product = productRepo.findById(prodId).orElseThrow(()->new ResourceNotFoundException("Product not found with id "+ prodId));
            Demand demand = demandRepo.findById(demandId).orElseThrow(()->new ResourceNotFoundException("Demand not found with demand id "+demandId));
            record.setDemandCount(quantity);
            record.setProduct(product);
            record.setDemand(demand);
            repo.save(record);
        }
    }

    public void recordView(Integer prodId){
        recordDemandEvent(prodId, Constants.VIEW,1);
    }

    public void recordAddToCart(Integer prodId){
        recordDemandEvent(prodId, Constants.ADD_TO_CART,1);
    }

    public void recordPurchase(Integer prodId, Integer quantity){
        inventoryService.updateProductCount(prodId,quantity);
        recordDemandEvent(prodId,Constants.PURCHASE,quantity);
    }

}
