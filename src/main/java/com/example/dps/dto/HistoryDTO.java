package com.example.dps.dto;

import com.example.dps.entity.PriceHistory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HistoryDTO {
    private Integer historyId;
    private BigDecimal basePrice;
    private BigDecimal discountedPrice;
    private BigDecimal compPrice;
    private LocalDateTime calculatedAt;
    private String triggeredBy;

    public static HistoryDTO createHistoryDTO(PriceHistory priceHistory){
        return new HistoryDTO(priceHistory.getHistoryId(),
                priceHistory.getBasePrice(),
                priceHistory.getDiscountedPrice(),
                priceHistory.getCompPrice(),
                priceHistory.getCalculatedAt(),
                priceHistory.getTriggeredBy());
    }
}
