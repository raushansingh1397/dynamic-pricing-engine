package com.example.dps.controller;

import com.example.dps.dto.HistoryDTO;
import com.example.dps.entity.PriceHistory;
import com.example.dps.service.PriceHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dynamicPricing/priceHistory")
@Slf4j
public class PriceHistoryController {
    private final PriceHistoryService priceService;

    @Autowired
    public PriceHistoryController(PriceHistoryService priceService) {
        this.priceService = priceService;
    }

    @GetMapping("/{prodId}")
    public ResponseEntity<?> getHistory(@PathVariable("prodId") int prodId){
        List<PriceHistory> history = priceService.getHistory(prodId);
        List<HistoryDTO> listDto = history.stream().map(HistoryDTO::createHistoryDTO).toList();
        log.info("history list of prod with id={} list={}",prodId,listDto.size());
        return ResponseEntity.ok(listDto);
    }
}
