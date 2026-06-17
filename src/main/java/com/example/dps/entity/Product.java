package com.example.dps.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "product")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer prodId;
    private String prodName;
    private String prodCategory;
    private String prodCompanyName;
    @Column(precision = 10, scale = 2)
    private BigDecimal basePrice;
    @Column(precision = 10, scale = 2)
    private BigDecimal currentDynamicPrice;
    @Column(precision = 10, scale = 2)
    private BigDecimal discountedPrice;
    private Boolean isActive;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL)

    private Inventory inventory;
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<PriceHistory> priceHistories;


}
