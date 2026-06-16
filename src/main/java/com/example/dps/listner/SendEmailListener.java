package com.example.dps.listner;

import com.example.dps.record.PriceChangeEvent;
import com.example.dps.service.SendMailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SendEmailListener {
    private final SendMailService mailService;
    @EventListener
    @Async
    public void sendEmail(PriceChangeEvent event) {
        // Simulate sending an email
        log.info("Sending email notification for product ID: {}. Price changed from {} to {} triggered by: {}",
                 event.productId(), event.oldPrice(), event.newPrice(), event.triggeredBy());

        mailService.sendEmail(event.productId(), event.oldPrice(), event.newPrice(), event.triggeredBy());
    }
}
