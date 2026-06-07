package com.example.dps.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inventory")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Inventory {
    @Id
    private Integer prodId;
    @MapsId
    @OneToOne
    @JoinColumn(name = "prod_id")
    private Product product;
    private Integer prodCount;
}
