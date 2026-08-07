package com.example.dps.service;

import com.example.dps.repository.PriceAlertRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendMailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private PriceAlertRepo priceAlertRepo;

    @InjectMocks
    private SendMailService sendMailService;

    @Test
    void testSendEmail_success() {
        // Arrange
        ReflectionTestUtils.setField(sendMailService, "fromEmail", "sender@example.com");
        ReflectionTestUtils.setField(sendMailService, "toEmail", "admin@example.com");

        int prodId = 1;
        BigDecimal oldPrice = new BigDecimal("100.00");
        BigDecimal newPrice = new BigDecimal("110.00");
        String triggeredBy = "COMPETITOR";

        // Act
        sendMailService.sendEmail(prodId, oldPrice, newPrice, triggeredBy);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals("sender@example.com", sentMessage.getFrom());
        assertEquals("admin@example.com", sentMessage.getTo()[0]);
        assertTrue(sentMessage.getSubject().contains("CRITICAL PRICE CHANGE ALERT"));
        assertTrue(sentMessage.getSubject().contains("1"));
        assertTrue(sentMessage.getText().contains("100"));
        assertTrue(sentMessage.getText().contains("110"));
    }

    @Test
    void testSendEmail_priceDecreased() {
        // Arrange
        ReflectionTestUtils.setField(sendMailService, "fromEmail", "sender@example.com");
        ReflectionTestUtils.setField(sendMailService, "toEmail", "admin@example.com");

        int prodId = 2;
        BigDecimal oldPrice = new BigDecimal("150.00");
        BigDecimal newPrice = new BigDecimal("120.00");
        String triggeredBy = "DYNAMIC_PRICING";

        // Act
        sendMailService.sendEmail(prodId, oldPrice, newPrice, triggeredBy);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertTrue(sentMessage.getText().contains("150"));
        assertTrue(sentMessage.getText().contains("120"));
    }

    @Test
    void testSendEmail_exception() {
        // Arrange
        ReflectionTestUtils.setField(sendMailService, "fromEmail", "sender@example.com");
        ReflectionTestUtils.setField(sendMailService, "toEmail", "admin@example.com");

        doThrow(new RuntimeException("SMTP error")).when(javaMailSender).send(any(SimpleMailMessage.class));

        int prodId = 3;
        BigDecimal oldPrice = new BigDecimal("100.00");
        BigDecimal newPrice = new BigDecimal("110.00");

        // Act - Should not throw, just log
        assertDoesNotThrow(() -> sendMailService.sendEmail(prodId, oldPrice, newPrice, "TEST"));

        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEmail_emailBodyFormat() {
        // Arrange
        ReflectionTestUtils.setField(sendMailService, "fromEmail", "sender@example.com");
        ReflectionTestUtils.setField(sendMailService, "toEmail", "admin@example.com");

        int prodId = 100;
        BigDecimal oldPrice = new BigDecimal("99.99");
        BigDecimal newPrice = new BigDecimal("89.99");
        String triggeredBy = "MARKET_CHANGE";

        // Act
        sendMailService.sendEmail(prodId, oldPrice, newPrice, triggeredBy);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage message = messageCaptor.getValue();
        String body = message.getText();
        assertTrue(body.contains("Product ID: 100"));
        assertTrue(body.contains("99.99"));
        assertTrue(body.contains("89.99"));
        assertTrue(body.contains("Dynamic Pricing Engine"));
    }

    @Test
    void testSendEmailManually_success() {
        // Arrange
        ReflectionTestUtils.setField(sendMailService, "fromEmail", "sender@example.com");
        ReflectionTestUtils.setField(sendMailService, "toEmail", "admin@example.com");

        Integer prodId = 1;

        // Act
        sendMailService.sendEmailManually(prodId);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals("sender@example.com", sentMessage.getFrom());
        assertEquals("admin@example.com", sentMessage.getTo()[0]);
        assertTrue(sentMessage.getSubject().contains("CRITICAL PRICE CHANGE ALERT"));
        assertTrue(sentMessage.getSubject().contains("1"));
        assertTrue(sentMessage.getText().contains("Product ID: %d"));
    }

    @Test
    void testSendEmailManually_exception() {
        // Arrange
        ReflectionTestUtils.setField(sendMailService, "fromEmail", "sender@example.com");
        ReflectionTestUtils.setField(sendMailService, "toEmail", "admin@example.com");

        doThrow(new RuntimeException("Mail server unreachable")).when(javaMailSender).send(any(SimpleMailMessage.class));

        Integer prodId = 5;

        // Act - Should not throw, just log
        assertDoesNotThrow(() -> sendMailService.sendEmailManually(prodId));

        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEmailManually_differentProductIds() {
        // Arrange
        ReflectionTestUtils.setField(sendMailService, "fromEmail", "sender@example.com");
        ReflectionTestUtils.setField(sendMailService, "toEmail", "admin@example.com");

        // Act
        for (int i = 1; i <= 5; i++) {
            sendMailService.sendEmailManually(i);
        }

        // Assert
        verify(javaMailSender, times(5)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEmail_largePrice() {
        // Arrange
        ReflectionTestUtils.setField(sendMailService, "fromEmail", "sender@example.com");
        ReflectionTestUtils.setField(sendMailService, "toEmail", "admin@example.com");

        int prodId = 999;
        BigDecimal oldPrice = new BigDecimal("9999.99");
        BigDecimal newPrice = new BigDecimal("10999.99");

        // Act
        sendMailService.sendEmail(prodId, oldPrice, newPrice, "MARKET");

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage message = messageCaptor.getValue();
        assertTrue(message.getText().contains("9999.99"));
        assertTrue(message.getText().contains("10999.99"));
    }

    @Test
    void testSendEmail_recipientEmail() {
        // Arrange
        ReflectionTestUtils.setField(sendMailService, "fromEmail", "noreply@pricing.com");
        ReflectionTestUtils.setField(sendMailService, "toEmail", "alerts@company.com");

        int prodId = 1;
        BigDecimal oldPrice = new BigDecimal("100.00");
        BigDecimal newPrice = new BigDecimal("110.00");

        // Act
        sendMailService.sendEmail(prodId, oldPrice, newPrice, "TEST");

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage message = messageCaptor.getValue();
        assertEquals("noreply@pricing.com", message.getFrom());
        assertEquals("alerts@company.com", message.getTo()[0]);
    }
}

