package com.example.dps.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "comp_pricing")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CompPricing {
    @EmbeddedId
    private CompPricingId id;
    @MapsId("prodId")
    @ManyToOne
    @JoinColumn(name="prod_id",insertable = false,updatable = false)
    private Product product;
    @MapsId("compId")
    @ManyToOne
    @JoinColumn(name = "comp_id",insertable = false,updatable = false)
    private Competitor competitor;
    @Column(precision = 10, scale = 2)
    private BigDecimal price;
    @Column(updatable = false)
    private LocalDateTime fetchedAt;

    @PrePersist
    protected void OnCreate(){
        this.fetchedAt = LocalDateTime.now();
    }
}
