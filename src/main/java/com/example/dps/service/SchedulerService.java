package com.example.dps.service;

import com.example.dps.dto.JobDTO;
import com.example.dps.entity.Product;
import com.example.dps.repository.ProductRepo;
import com.example.dps.utils.Constants;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class SchedulerService {
    private final PricingEngine pricingEngine;
    private final ProductService productService;
    private final JobLogService jobLogService;
    private final ProductRepo productRepo;

    @PersistenceContext
    private EntityManager entityManager;

    @Scheduled(fixedRateString = "${pricing.scheduler.interval:3600000}")
    @Transactional
    public void scheduleDynamicPricing(){
        JobDTO job = new JobDTO();
        int productsUpdated = 0;
        int totalActiveProducts = 0;

        int pageNumber = 0;
        int batchSize = 1000;
        boolean hasMorePages = true;

        while(hasMorePages){
            Pageable pageable = PageRequest.of(pageNumber,batchSize);
            Page<Product> productPage = productRepo.findAllActiveProducts(pageable);

            if(pageNumber == 0){
                totalActiveProducts = (int) productPage.getTotalElements();
                if(totalActiveProducts == 0){
                    job.setRunAt(LocalDateTime.now());
                    job.setProductsUpdated(0);
                    job.setStatus("NO_PRODUCTS");
                    jobLogService.updateLogs(job);
                    return;
                }
            }
            for(Product dto:productPage.getContent()){
                try{
                    BigDecimal price = pricingEngine.calculateDynamicPrice(dto);
                    productService.updateProductPrice(dto.getProdId(),price);
                    productsUpdated++;

                } catch (Exception e){
                    log.error("Exception occurred while processing product id={} reason={} ",dto.getProdId(), e.getMessage());
                }
            }
            entityManager.flush();
            entityManager.clear();

            if(productPage.hasNext()){
                pageNumber++;
            } else {
                hasMorePages = false;
            }
        }

        job.setRunAt(LocalDateTime.now());
        job.setProductsUpdated(productsUpdated);
        if(productsUpdated == 0){
            job.setStatus("FAILED");
        } else if (productsUpdated < totalActiveProducts){
            job.setStatus("PARTIAL");
        } else {
            job.setStatus("SUCCESS");
        }
        jobLogService.updateLogs(job);

    }

}
