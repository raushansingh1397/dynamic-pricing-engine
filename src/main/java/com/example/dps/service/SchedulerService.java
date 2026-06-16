package com.example.dps.service;

import com.example.dps.dto.JobDTO;
import com.example.dps.entity.Product;
import com.example.dps.repository.ProductRepo;
import com.example.dps.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
public class SchedulerService {
    private final PricingEngine pricingEngine;
    private final ProductService productService;
    private final JobLogService jobLogService;
    private final ProductRepo productRepo;

    @Autowired
    public SchedulerService(PricingEngine pricingEngine, ProductService productService, JobLogService jobLogService, ProductRepo productRepo) {
        this.pricingEngine = pricingEngine;
        this.productService = productService;
        this.jobLogService = jobLogService;
        this.productRepo = productRepo;
    }

    @Scheduled(fixedRateString = "${pricing.scheduler.interval:3600000}")
    public void scheduleDynamicPricing(){
        List<JobDTO> list = new ArrayList<>();
        JobDTO job = new JobDTO();
        int productsUpdated = 0;
        List<Product> activeProductList = productRepo.findAllActiveProducts();
        for(Product dto:activeProductList){
            try{
                BigDecimal price = pricingEngine.calculateDynamicPrice(dto);
                productService.updateProductPrice(dto.getProdId(),price);
                productsUpdated++;

            } catch (Exception e){
                log.error("Exception occurred while processing product id={} reason={} ",dto.getProdId(), e.getMessage());
            }
        }
        job.setRunAt(LocalDateTime.now());
        job.setProductsUpdated(productsUpdated);
        if(activeProductList.isEmpty()){
            job.setStatus("NO_PRODUCTS");
        } else if(productsUpdated == 0){
            job.setStatus("FAILED");
        } else if (productsUpdated < activeProductList.size()){
            job.setStatus("PARTIAL");
        } else {
            job.setStatus("SUCCESS");
        }
        jobLogService.updateLogs(job);

    }

}
