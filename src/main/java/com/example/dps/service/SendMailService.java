package com.example.dps.service;

import com.example.dps.dto.EmailDTO;
import com.example.dps.entity.PriceAlert;
import com.example.dps.repository.PriceAlertRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendMailService {
    private final JavaMailSender javaMailSender;
    private final PriceAlertRepo priceAlertRepo;
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${pricing.alert.recipient-email}")
    private String toEmail;


    public void sendEmail(int prodId, BigDecimal oldPrice, BigDecimal newPrice, String triggeredBy) {
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setFrom(fromEmail);
            message.setSubject("🚨 CRITICAL PRICE CHANGE ALERT: Product ID " + prodId);
            String emailBody = String.format(
                    """
                            Hello Admin,
                            
                            A significant price fluctuation has occurred.
                            
                            Product ID: %d
                            Old Price: $%s
                            New Dynamic Price: $%s
                            
                            Please verify this change in the management dashboard.
                            
                            Best regards,
                            Dynamic Pricing Engine""",
                    prodId, oldPrice.toString(), newPrice.toString()
            );
            message.setText(emailBody);
            javaMailSender.send(message);
            log.info("Price alert mail sent for product ID {}, with old price {}, and new price {} ",prodId,oldPrice,newPrice);
        } catch (Exception e){
            log.info("Failed to send price alert email for product ID {}. Reason {}", prodId,e.getMessage());
        }
    }

    // from controller
    // TODO can be used to send mail from the controller, proper implementation required
    public void sendEmailManually(Integer prodId) {
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setSubject("🚨 CRITICAL PRICE CHANGE ALERT: Product ID " + prodId);
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            String emailBody = """
                    Hello Admin,
                   
                    A Significant price fluctuation has occurred.
                   
                    Product ID: %d
                   
                    Please verify this change in the management dashboard.
                           
                    Best regards,
                    Dynamic Pricing Engine""";
            message.setText(emailBody);
            javaMailSender.send(message);
            log.info("Mail sent successfully !!");
        } catch (Exception e){
            log.info("Failed to send the test mail!");
        }
    }
}
