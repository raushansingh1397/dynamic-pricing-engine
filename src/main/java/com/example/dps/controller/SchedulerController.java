package com.example.dps.controller;

import com.example.dps.service.SchedulerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dynamicPricing/scheduler")
@Slf4j
public class SchedulerController {
    private final SchedulerService service;

    @Autowired
    public SchedulerController(SchedulerService service) {
        this.service = service;
    }

    @PostMapping("/trigger")
    public ResponseEntity<String> scheduleDynamicPricing(){
        log.info("Manual pricing triggered initiated");
        service.scheduleDynamicPricing();
        log.info("Manual Pricing triggered completed");
        return ResponseEntity.ok("Scheduler started successfully");
    }
}
