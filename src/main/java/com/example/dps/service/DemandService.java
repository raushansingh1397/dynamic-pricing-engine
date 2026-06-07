package com.example.dps.service;

import com.example.dps.exception.ResourceNotFoundException;
import com.example.dps.repository.DemandRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DemandService {
    private final DemandRepo demandRepo;

    @Autowired
    public DemandService(DemandRepo demandRepo) {
        this.demandRepo = demandRepo;
    }

    public Integer getDemandScore(Integer demandId){
        return demandRepo.findById(demandId)
                .orElseThrow(()-> new ResourceNotFoundException("Demand type not found!"))
                .getDemandValue();
    }
}
