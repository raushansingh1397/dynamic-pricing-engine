package com.example.dps.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RuleContext {
    private double demandScore;
    private int stockCount;
    private double competitorPriceDiff;
}
