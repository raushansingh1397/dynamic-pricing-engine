package com.example.dps.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_history")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PriceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer historyId;
    @ManyToOne
    @JoinColumn(name = "prod_id")
    private Product product;
    @Column(precision = 10, scale = 2)
    private BigDecimal basePrice;
    @Column(precision = 10, scale = 2)
    private BigDecimal discountedPrice;
    @Column(precision = 10, scale = 2)
    private BigDecimal compPrice;
    private LocalDateTime calculatedAt;
    private String triggeredBy;

}
