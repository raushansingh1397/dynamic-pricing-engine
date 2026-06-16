package com.example.dps.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "price_alert")
@AllArgsConstructor
@NoArgsConstructor
public class PriceAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer alertId;
    @ManyToOne
    @JoinColumn(name = "prod_id", nullable = false)
    private Product product;
    private String alertType; // e.g., "PRICE_DROP", "PRICE_INCREASE"
    private String triggeredBy; // e.g., "SYSTEM", "USER"
    private BigDecimal newPrice;
    private BigDecimal oldPrice;
    private Instant triggeredAt;
    private BigDecimal basePrice;

    @PrePersist
    protected void OnCreate() {
        this.triggeredAt = Instant.now();
    }

}
