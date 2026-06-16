package com.example.dps.controller;

import com.example.dps.dto.EmailDTO;
import com.example.dps.service.SendMailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dynamicPricing/alerts")
@RequiredArgsConstructor
public class AlertController {
    private final SendMailService emailService;

    @PostMapping("/{}/notify")
    public ResponseEntity<String> sendMail(@PathVariable("prodId") Integer prodId){
        emailService.sendEmailManually(prodId);
        return ResponseEntity.ok("Mail sent successfully!!");
    }
}
