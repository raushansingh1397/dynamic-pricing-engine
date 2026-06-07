package com.example.dps.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "track_prod_demand")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrackProdDemand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer trackId;

    @ManyToOne
    @JoinColumn(name = "prod_id")
    private Product product;
    @ManyToOne
    @JoinColumn(name = "demand_id")
    private Demand demand;
    private int demandCount;
    @Column(updatable = false)
    private LocalDateTime recordedAt;
    @PrePersist
    protected void OnCreate(){
        this.recordedAt = LocalDateTime.now();
    }
}
